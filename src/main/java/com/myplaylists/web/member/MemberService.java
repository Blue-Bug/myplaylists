package com.myplaylists.web.member;

import com.myplaylists.domain.Member;
import com.myplaylists.web.config.AppProperties;
import com.myplaylists.web.mail.EmailContent;
import com.myplaylists.web.mail.EmailService;
import com.myplaylists.web.member.form.LoginForm;
import com.myplaylists.web.member.form.SignUpForm;
import com.myplaylists.web.setting.form.PasswordForm;
import com.myplaylists.web.setting.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    public Member joinProcess(SignUpForm signUpForm) {
        Member newMember = saveNewMember(signUpForm);

        sendVerifyingEmail(newMember);

        return newMember;
    }

    private void sendVerifyingEmail(Member member) {
        Context context = new Context();

        context.setVariable("link","/email-verify?token="+member.getToken()+"&email="+member.getEmail());
        context.setVariable("nickname",member.getNickname());
        context.setVariable("linkName","이메일 인증 링크");
        context.setVariable("message","링크를 클릭하여 이메일 인증을 완료해주세요.");
        context.setVariable("host",appProperties.getHost());

        String message = templateEngine.process("mail/verify-link", context);

        EmailContent emailContent = new EmailContent();
        emailContent.setTo(member.getEmail());
        emailContent.setSubject("My Playlists 가입 인증 이메일입니다.");
        emailContent.setBody(message);

        emailService.sendEmail(emailContent);
    }

    private Member saveNewMember(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));

        Member member = modelMapper.map(signUpForm, Member.class);
        member.generateToken();

        return memberRepository.save(member);
    }

    public void login(LoginForm loginForm) {
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                            loginForm.getNickname(),
                            loginForm.getPassword(),
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));

            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }catch (DisabledException | LockedException | BadCredentialsException e){
            if(e instanceof DisabledException){
                log.info("로그인 중 계정이 비활성화 된 경우 발생");
            }
            else if(e instanceof LockedException){
                log.info("로그인 중 계정이 잠기게 된 경우 발생");
            }
            else if(e instanceof BadCredentialsException){
                log.info("로그인 중 비밀번호가 불일치 된 경우 발생");
            }
        }
    }
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByNickname(username);
        if(member == null){
            member = memberRepository.findByEmail(username);
            if(member == null){
                throw new UsernameNotFoundException(username);
            }
        }

        return new UserMember(member);
    }

    public Member emailVerification(String email, String token) {
        Member member = memberRepository.findByEmail(email);
        if(member == null){
            return null;
        }
        if(!member.getToken().equals(token)){
            return null;
        }
        if(!member.isEmailVerified()){
            member.checkEmailVerified();
        }
        return member;
    }

    public boolean updateProfile(Member member, ProfileForm profileForm) {
        Member byNickname = memberRepository.findByNickname(member.getNickname());
        if(byNickname == null){
            return false;
        }
        byNickname.setIntroduce(profileForm.getIntroduce());
        memberRepository.save(byNickname);
        return true;
    }

    public boolean signOut(Member member) {
        Member byNickname = memberRepository.findByNickname(member.getNickname());
        if(byNickname == null){
            return false;
        }
        memberRepository.delete(member);
        return true;
    }

    public boolean updatePassword(Member member, PasswordForm passwordForm) {
        Member byNickname = memberRepository.findByNickname(member.getNickname());
        if(byNickname == null){
            return false;
        }
        byNickname.setPassword(passwordEncoder.encode(passwordForm.getPassword()));
        memberRepository.save(byNickname);
        return true;
    }

    public void logout(HttpServletRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated()){
            new SecurityContextLogoutHandler().logout(req,null,auth);
        }
    }
}
