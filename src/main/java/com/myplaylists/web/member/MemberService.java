package com.myplaylists.web.member;

import com.myplaylists.domain.Member;
import com.myplaylists.web.config.SecurityConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;

    public Member joinProcess(SignUpForm signUpForm) {
        Member newMember = saveNewMember(signUpForm);

        sendVerifyingEmail(newMember);

        return newMember;
    }

    private void sendVerifyingEmail(Member member) {
        log.info("/email-verify?email={}&token={}",member.getEmail(),member.getToken());
    }

    private Member saveNewMember(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));

        Member member = modelMapper.map(signUpForm, Member.class);
        member.generateToken();

        return memberRepository.save(member);
    }

    public void login(Member member) {
        try {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(new UserMember(member), member.getPassword());
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }catch (DisabledException | LockedException | BadCredentialsException e){
            if(e instanceof DisabledException){
                log.info("회원 가입 후 로그인 중 계정이 비활성화 된 경우 발생");
            }
            else if(e instanceof LockedException){
                log.info("회원 가입 후 로그인 중 계정이 잠기게 된 경우 발생");
            }
            else if(e instanceof BadCredentialsException){
                log.info("회원 가입 후 로그인 중 비밀번호가 불일치 된 경우 발생");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByNickname(username);
        if(member == null){
            member = memberRepository.findByEmail(username);
        }
        if(member == null){
            throw new UsernameNotFoundException(username);
        }
        return new UserMember(member);
    }
}
