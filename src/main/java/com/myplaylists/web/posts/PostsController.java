package com.myplaylists.web.posts;

import com.myplaylists.domain.Member;
import com.myplaylists.domain.Posts;
import com.myplaylists.web.member.CurrentUser;
import com.myplaylists.web.posts.form.PostsForm;
import com.myplaylists.web.posts.validator.PlaylistsFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PostsController {

    private final PostsService postsService;

    @GetMapping("/posts/create")
    public String createPostsForm(@CurrentUser Member member, Model model){
        PostsForm postsForm = new PostsForm();
        model.addAttribute(member);
        model.addAttribute("postsForm",postsForm);
        return "posts/create";
    }

    @PostMapping("/posts/create")
    public String createPosts(@CurrentUser Member member, @Valid PostsForm postsForm, Errors errors){
        if(errors.hasErrors()){
            return "posts/create";
        }
        Posts newPosts = postsService.addPosts(postsForm,member);
        return "redirect:/posts/" + newPosts.getId();
    }

    @GetMapping("/posts/{id}")
    public String readPosts(@CurrentUser Member member, @PathVariable("id") String postsId, Model model){
        Optional<Posts> posts = postsService.getPosts(postsId);
        if(posts.isEmpty()){
            model.addAttribute("error","존재하지 않는 포스트 입니다.");
            return "redirect:/";
        }
        model.addAttribute("posts",posts.get());
        return "posts/read";
    }

    @GetMapping("/posts/{id}/edit")
    public String editPostsForm(){
        return "posts/edit";
    }

    @PostMapping("/posts/{id}/edit")
    public String editPosts(){
        //update
        return "";
    }

    @PostMapping("/posts/{id}/remove")
    public String removePosts(){
        return "redirect:/";
    }

}
