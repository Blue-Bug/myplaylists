package com.myplaylists.web.main;

import com.myplaylists.domain.Member;
import com.myplaylists.domain.Posts;
import com.myplaylists.web.member.CurrentUser;
import com.myplaylists.web.posts.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PostsService postsService;

    @GetMapping("/")
    public String home(@CurrentUser Member member, Model model) {
        if(member != null){
            model.addAttribute(member);
        }
        List<Posts> allPosts = postsService.getAllPosts();
        if(!allPosts.isEmpty()){
            model.addAttribute("posts",allPosts);
        }
        return "home";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }
}
