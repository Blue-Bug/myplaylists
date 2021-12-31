package com.myplaylists.web.main;

import com.myplaylists.domain.Member;
import com.myplaylists.web.member.CurrentUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/")
    public String home(@CurrentUser Member member, Model model) {
        if(member != null){
            model.addAttribute(member);
        }
        return "home";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }
}
