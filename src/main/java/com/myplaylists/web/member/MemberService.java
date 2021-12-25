package com.myplaylists.web.member;

import com.myplaylists.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

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
}
