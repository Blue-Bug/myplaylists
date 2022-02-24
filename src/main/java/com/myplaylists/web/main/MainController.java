package com.myplaylists.web.main;

import com.myplaylists.domain.Member;
import com.myplaylists.domain.Posts;
import com.myplaylists.web.member.CurrentUser;
import com.myplaylists.web.posts.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PostsService postsService;

    @GetMapping("/")
    public String home(@CurrentUser Member member, Model model, Pageable pageable) {
        Page<Posts> allPosts = postsService.getAllPosts(pageable);

        if(!allPosts.getContent().isEmpty()){
            model.addAttribute("posts",allPosts.getContent());
            model.addAttribute("totalPage",allPosts.getTotalPages());
        }
        return "home";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam String keyword, Model model, Pageable pageable){
        Page<Posts> filteredPosts = postsService.searchPosts(keyword,pageable);

        if(!filteredPosts.getContent().isEmpty()){
            model.addAttribute("posts",filteredPosts.getContent());
            model.addAttribute("totalPage",filteredPosts.getTotalPages());
        }
        return "home";
    }
}
