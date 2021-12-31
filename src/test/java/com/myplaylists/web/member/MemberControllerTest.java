package com.myplaylists.web.member;

import com.myplaylists.domain.Member;
import com.myplaylists.web.member.form.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberService memberService;

    @BeforeEach
    void BeforeEach(){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("jun");
        signUpForm.setEmail("test@test.com");
        signUpForm.setPassword("123123123");
        memberService.joinProcess(signUpForm);
    }

    @AfterEach
    void AfterEach(){
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 화면")
    void signUpPage() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/sign-up"));
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signUp_Success() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname","jun1")
                        .param("email","test1@test.com")
                        .param("password","123123123")
                        .param("passwordConfirm","123123123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(view().name("redirect:/"));

        Member member = memberRepository.findByNickname("jun1");
        assertEquals("test1@test.com",member.getEmail());
    }

    @Test
    @DisplayName("회원가입 - 실패")
    void signUp_Failure() throws Exception {
        //닉네임 길이
        mockMvc.perform(post("/sign-up")
                        .param("nickname","ju")
                        .param("email","test@test.com")
                        .param("password","123123123")
                        .param("passwordConfirm","123123123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/sign-up"));

        Member member1 = memberRepository.findByNickname("ju");
        assertNull(member1);

        //이메일 형식
        mockMvc.perform(post("/sign-up")
                        .param("nickname","jun1")
                        .param("email","testtest.com")
                        .param("password","123123123")
                        .param("passwordConfirm","123123123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/sign-up"));


        Member member2 = memberRepository.findByNickname("jun1");
        assertNull(member2);

        //비밀번호 길이
        mockMvc.perform(post("/sign-up")
                        .param("nickname","jun1")
                        .param("email","test@test.com")
                        .param("password","12313")
                        .param("passwordConfirm","12313")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/sign-up"));


        Member member3 = memberRepository.findByNickname("jun1");
        assertNull(member3);



        //비밀번호 불일치
        mockMvc.perform(post("/sign-up")
                        .param("nickname","jun1")
                        .param("email","test@test.com")
                        .param("password","123123123")
                        .param("passwordConfirm","1231231223")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/sign-up"));


        Member member4 = memberRepository.findByNickname("jun1");
        assertNull(member4);

        //빈칸
        mockMvc.perform(post("/sign-up")
                        .param("nickname","")
                        .param("email","")
                        .param("password","")
                        .param("passwordConfirm","")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/sign-up"));

        Member member5 = memberRepository.findByNickname("");
        assertNull(member5);

        //이미 존재하는 이메일
        mockMvc.perform(post("/sign-up")
                        .param("nickname","jun1")
                        .param("email","test@test.com")
                        .param("password","123123123")
                        .param("passwordConfirm","123123123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/sign-up"));


        Member member6 = memberRepository.findByNickname("jun1");
        assertNull(member6);

        //이미 존재하는 닉네임
        mockMvc.perform(post("/sign-up")
                        .param("nickname","jun")
                        .param("email","test1@test.com")
                        .param("password","123123123")
                        .param("passwordConfirm","123123123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/sign-up"));


        Member member7 = memberRepository.findByNickname("jun");
        assertNotEquals("test1@test.com",member7.getEmail());
    }

    @Test
    @DisplayName("로그인 화면")
    void loginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("로그인 - 성공")
    void loginSuccess() throws Exception{
        mockMvc.perform(post("/login")
                .param("username","jun")
                .param("password","123123123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated());
    }

    @Test
    @DisplayName("로그인 - 실패")
    void loginFailure() throws Exception {
        //계정이 없는 경우
        mockMvc.perform(post("/login")
                        .param("username","jun1")
                        .param("password","123123123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());

        //비밀번호가 틀린 경우
        mockMvc.perform(post("/login")
                    .param("username","jun")
                    .param("password","12312312")
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("이메일 인증 - 성공")
    void emailVerifySuccess() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        mockMvc.perform(get("/email-verify")
                    .param("email",member.getEmail())
                    .param("token",member.getToken()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(view().name("member/email-verify"));

        member = memberRepository.findByNickname("jun");
        assertTrue(member.isEmailVerified());
    }

    @Test
    @DisplayName("이메일 인증 - 실패")
    void emailVerifyFailure() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        //이메일 주소 불일치
        mockMvc.perform(get("/email-verify")
                .param("email","test")
                .param("token", member.getToken()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeDoesNotExist("member"))
                .andExpect(view().name("member/email-verify"));

        //토큰 불일치
        mockMvc.perform(get("/email-verify")
                        .param("email","test")
                        .param("token", member.getToken()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeDoesNotExist("member"))
                .andExpect(view().name("member/email-verify"));

        member = memberRepository.findByNickname("jun");
        assertFalse(member.isEmailVerified());
    }
}