package com.myplaylists.web.posts;

import com.fasterxml.jackson.core.json.UTF8DataInputJsonParser;
import com.myplaylists.domain.Comment;
import com.myplaylists.domain.Member;
import com.myplaylists.domain.Posts;
import com.myplaylists.web.member.CurrentUser;
import com.myplaylists.web.posts.form.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostsController {

    private final PostsService postsService;
    private final ModelMapper modelMapper;

    @GetMapping("/create")
    public String createPostsForm(@CurrentUser Member member, Model model){
        PostsForm postsForm = new PostsForm();
        model.addAttribute("postsForm",postsForm);
        return "posts/create";
    }

    @PostMapping("/create")
    public String createPosts(@CurrentUser Member member, @Valid PostsForm postsForm, Errors errors){
        if(errors.hasErrors()){
            return "posts/create";
        }
        Posts newPosts = postsService.addPosts(postsForm,member);
        return "redirect:/posts/" + newPosts.getId();
    }

    @GetMapping("/{id}")
    public String readPosts(@CurrentUser Member member, @PathVariable("id") String postsId, Model model){
        Optional<Posts> posts = postsService.getPosts(postsId);
        if(posts.isEmpty()){
            model.addAttribute("error","존재하지 않는 게시물입니다.");
            return "redirect:/";
        }
        if(posts.get().getPostsOwner().equals(member)){
            model.addAttribute("owner",true);
        }
        model.addAttribute("posts",posts.get());

        return "posts/read";
    }

    @PostMapping("/{id}/remove")
    public String removePosts(@CurrentUser Member member, @PathVariable("id") String postsId, RedirectAttributes redirectAttributes){
        if(!postsService.deletePosts(member,postsId)){
            redirectAttributes.addFlashAttribute("error","잘못된 요청입니다.");
        }
        return "redirect:/";
    }

    @GetMapping("/{id}/edit")
    public String editPostsForm(@CurrentUser Member member, @PathVariable("id") String postsId, Model model, RedirectAttributes redirectAttributes){
        Optional<Posts> findPosts = postsService.getPosts(postsId);

        if(findPosts.isEmpty()){
            redirectAttributes.addFlashAttribute("error","존재하지 않는 게시물입니다.");
            return "redirect:/";
        }

        Posts posts = findPosts.get();

        if(!posts.getPostsOwner().getNickname().equals(member.getNickname())){
            redirectAttributes.addFlashAttribute("error","게시물의 소유자가 아닙니다.");
            return "redirect:/posts/"+postsId;
        }

        PostsEditForm postsEditForm = modelMapper.map(posts, PostsEditForm.class);

        //Posts 의 Playlist 와 Link 를 전부 PlaylistEditForm 에 담아서 List 로 가져옴
        List<PlaylistsEditForm> playlistsEditForms = posts.getPlaylists()
                .stream()
                .map(playlist -> {
                    PlaylistsEditForm playlistsEditForm = modelMapper.map(playlist, PlaylistsEditForm.class);
                    playlistsEditForm.setLinks(playlist.getLinks().stream().map(l -> l.getLink()).collect(Collectors.toList()));
                    return playlistsEditForm;})
                .collect(Collectors.toList());

        postsEditForm.setPlaylistsEditForms(playlistsEditForms);

        model.addAttribute("postsId",postsId);
        model.addAttribute("postsEditForm",postsEditForm);

        return "posts/edit";
    }

    @PostMapping("/{id}/edit")
    public String editPosts(@CurrentUser Member member, @PathVariable("id") String postsId, @Valid PostsEditForm postsEditForm,
                            Errors errors, Model model, RedirectAttributes redirectAttributes){
        if(errors.hasErrors()){
            model.addAttribute("postsId",postsId);
            model.addAttribute("postsEditForm",postsEditForm);
            return "posts/edit";
        }
        Optional<Posts> findPosts = postsService.getPosts(postsId);

        if(findPosts.isEmpty()){
            redirectAttributes.addFlashAttribute("error","존재하지 않는 게시물입니다.");
            return "redirect:/";
        }

        Posts posts = findPosts.get();

        if(!posts.getPostsOwner().getNickname().equals(member.getNickname())){
            redirectAttributes.addFlashAttribute("error","게시물의 소유자가 아닙니다.");
            return "redirect:/posts/"+postsId;
        }

        try {
            postsService.updatePosts(posts,postsEditForm);
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("error","잘못된 요청입니다.");
        }

        return "redirect:/posts/"+postsId;
    }


    @ResponseBody
    @GetMapping("/{id}/comment")
    public ResponseEntity readComment(@PathVariable("id") String postsId){
        Map<String,String> result = new HashMap<>();
        Optional<Posts> posts = postsService.getPosts(postsId);

        if(posts.isEmpty()){
            result.put("errors","Posts가 존재하지 않습니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        List<CommentForm> commentForms = getCommentFormList(posts);

        return ResponseEntity.ok().body(commentForms);
    }

    @ResponseBody
    @PostMapping("/{id}/comment")
    public ResponseEntity addComment(@CurrentUser Member member, @PathVariable("id") String postsId,
                                     @RequestBody @Valid CommentAddForm commentAddForm, Errors errors){
        Map<String,String> result = new HashMap<>();

        if(errors.hasErrors()){
            result.put("errors",errors.getFieldError().getDefaultMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        Optional<Posts> posts = postsService.getPosts(postsId);

        if(posts.isEmpty()){
            result.put("errors","Posts가 존재하지 않습니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        postsService.addComment(member,posts.get(),commentAddForm.getContent());

        List<CommentForm> commentForms = getCommentFormList(posts);

        return ResponseEntity.created(URI.create("/posts/"+postsId+"/comment")).body(commentForms);
    }

    @ResponseBody
    @DeleteMapping("/{id}/comment")
    public ResponseEntity removeComment(@CurrentUser Member member,@PathVariable("id") String postsId,
                                        @RequestBody CommentForm commentForm){
        Map<String,String> result = new HashMap<>();
        Optional<Posts> posts = postsService.getPosts(postsId);

        if(posts.isEmpty()){
            result.put("errors","Posts가 존재하지 않습니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        Optional<Comment> comment = postsService.getComment(commentForm.getId());

        if(comment.isEmpty()){
            result.put("errors","Comment가 존재하지 않습니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        if(!comment.get().getCommentedPosts().equals(posts.get())){
            result.put("errors","잘못된 요청입니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        if(!comment.get().getWrittenMember().getNickname().equals(member.getNickname())){
            result.put("errors","자신이 작성한 Comment만 삭제 할 수 있습니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        postsService.removeComment(comment.get());

        List<CommentForm> commentForms = getCommentFormList(posts);

        return ResponseEntity.ok().body(commentForms);
    }

    @ResponseBody
    @PatchMapping("/{id}/comment")
    public ResponseEntity updateComment(@CurrentUser Member member,@PathVariable("id") String postsId,
                                        @RequestBody @Valid CommentForm commentForm, Errors errors){
        Map<String,String> result = new HashMap<>();

        if(errors.hasErrors()){
            result.put("errors",errors.getFieldError().getDefaultMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        Optional<Posts> posts = postsService.getPosts(postsId);

        if(posts.isEmpty()){
            result.put("errors","Posts가 존재하지 않습니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        Optional<Comment> comment = postsService.getComment(commentForm.getId());

        if(comment.isEmpty()){
            result.put("errors","Comment가 존재하지 않습니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        if(!comment.get().getCommentedPosts().equals(posts.get())){
            result.put("errors","잘못된 요청입니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        if(!comment.get().getWrittenMember().getNickname().equals(member.getNickname())){
            result.put("errors","자신이 작성한 Comment만 수정 할 수 있습니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(result);
        }

        postsService.updateComment(comment.get(),commentForm);

        List<CommentForm> commentForms = getCommentFormList(posts);

        return ResponseEntity.ok().body(commentForms);
    }

    private List<CommentForm> getCommentFormList(Optional<Posts> posts) {
        return postsService.getAllComment(posts.get()).stream()
                .map(c -> {
                    CommentForm map = modelMapper.map(c, CommentForm.class);
                    map.setCreatedAt(c.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss")));
                    map.setModifiedAt(c.getModifiedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss")));
                    return map;
                })
                .collect(Collectors.toList());
    }
}
