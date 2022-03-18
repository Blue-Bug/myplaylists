package com.myplaylists.web.setting;

import com.myplaylists.domain.Member;
import com.myplaylists.web.member.CurrentUser;
import com.myplaylists.web.member.MemberService;
import com.myplaylists.web.setting.form.PasswordForm;
import com.myplaylists.web.setting.form.ProfileForm;
import com.myplaylists.web.setting.validator.PasswordFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingController {

    private final MemberService memberService;
    private final PasswordFormValidator passwordFormValidator;
    private final ModelMapper modelMapper;

    @InitBinder("passwordForm")
    public void passwordFormValidator(WebDataBinder webDataBinder){
        webDataBinder.addValidators(passwordFormValidator);
    }

    @GetMapping("/setting/profile")
    public String profileForm(@CurrentUser Member member, Model model){
        ProfileForm profileForm = modelMapper.map(member, ProfileForm.class);
        model.addAttribute(member);
        model.addAttribute(profileForm);
        return "setting/profile";
    }

    @PostMapping("/setting/profile")
    public String profileUpdate(@CurrentUser Member member, @Valid ProfileForm profileForm, Errors errors, Model model
                                , RedirectAttributes redirectAttributes){
        if(errors.hasErrors()){
            return "setting/profile";
        }
        if(memberService.updateProfile(member,profileForm)){
            redirectAttributes.addFlashAttribute("message","프로필을 수정했습니다.");
        }
        else{
            redirectAttributes.addFlashAttribute("message","프로필을 수정 중 문제가 발생했습니다. 다시 시도해주세요.");
        }

        return "redirect:/setting/profile";
    }

    @GetMapping("/setting/password")
    public String updatePasswordForm(@CurrentUser Member member, Model model){
        model.addAttribute(member);
        model.addAttribute(new PasswordForm());
        return "setting/password";
    }

    @PostMapping("/setting/password")
    public String updatePassword(@CurrentUser Member member, @Valid PasswordForm passwordForm, Errors errors,RedirectAttributes redirectAttributes){
        if(errors.hasErrors()){
            return "setting/password";
        }
        if(memberService.updatePassword(member,passwordForm)){
            redirectAttributes.addFlashAttribute("message","비밀번호를 수정했습니다.");
        }
        else{
            redirectAttributes.addFlashAttribute("message","비밀번호 수정 중 문제가 발생했습니다. 다시 시도해주세요.");
        }

        return "redirect:/setting/password";
    }

    @GetMapping("/setting/sign-out")
    public String signOutForm(@CurrentUser Member member,Model model){
        model.addAttribute(member);
        return "setting/sign-out";
    }

    @PostMapping("/setting/sign-out")
    public String signOut(@CurrentUser Member member, HttpServletRequest req, Model model,RedirectAttributes redirectAttributes){
        if(!memberService.signOut(member)){
            model.addAttribute(member);
            model.addAttribute("message","회원 탈퇴 중 문제가 발생했습니다.");
            return "setting/sign-out";
        }
        memberService.logout(req);
        redirectAttributes.addFlashAttribute("message","정상적으로 회원 탈퇴 되었습니다.");
        return "redirect:/";
    }
}
