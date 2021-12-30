package com.myplaylists.web.member;

import com.myplaylists.domain.Member;
import com.myplaylists.web.member.form.LoginForm;
import com.myplaylists.web.member.form.SignUpForm;
import com.myplaylists.web.member.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final SignUpFormValidator signUpFormValidator;
    private final MemberService memberService;

    @InitBinder("signUpForm")
    public void signUpFormValidator(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model){
        model.addAttribute(new SignUpForm());
        return "member/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@Valid SignUpForm signUpForm, Errors errors){
        if(errors.hasErrors()){
            return "member/sign-up";
        }
        LoginForm loginForm = new LoginForm(signUpForm.getNickname(), signUpForm.getPassword());

        memberService.joinProcess(signUpForm);

        memberService.login(loginForm);

        return "redirect:/";
    }

    @GetMapping("/email-verify")
    public String emailVerify(@RequestParam String email,@RequestParam String token,Model model){
        Member member = memberService.emailVerification(email, token);
        if(member == null){
            model.addAttribute("error","invalid.verify");
            return "member/email-verify";
        }
        model.addAttribute(member);
        return "member/email-verify";
    }
}
