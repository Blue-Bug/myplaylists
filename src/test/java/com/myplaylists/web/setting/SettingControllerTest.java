package com.myplaylists.web.setting;

import com.myplaylists.domain.Member;
import com.myplaylists.web.member.MemberRepository;
import com.myplaylists.web.member.MemberService;
import com.myplaylists.web.member.form.SignUpForm;
import com.myplaylists.web.setting.form.PasswordForm;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.event.annotation.BeforeTestExecution;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

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
    @DisplayName("프로필 화면")
    void profilePage() throws Exception {
        mockMvc.perform(get("/profile/jun"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"))
                .andExpect(view().name("member/profile"));
    }

    @Test
    @DisplayName("존재하지 않는 계정 프로필 화면")
    void profilePageNotExist() throws Exception {
        mockMvc.perform(get("/profile/jun1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("notExist",true))
                .andExpect(model().attributeDoesNotExist("member"))
                .andExpect(view().name("member/profile"));
    }

    @Test
    @DisplayName("계정 주인의 프로필 화면")
    @WithUserDetails(value="jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void profilePageOwner() throws Exception{
        mockMvc.perform(get("/profile/jun"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("owner",true))
                .andExpect(model().attributeExists("member"))
                .andExpect(view().name("member/profile"));
    }

    @Test
    @DisplayName("인증되지 않은 프로필 변경 화면 접근")
    void unauthenticatedUpdateProfilePage() throws Exception {
        mockMvc.perform(get("/setting/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("프로필 변경 화면")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateProfilePage() throws Exception {
        mockMvc.perform(get("/setting/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(view().name("setting/profile"));
    }

    @Test
    @DisplayName("프로필 변경 - 성공")
    @WithUserDetails(value="jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateProfileSuccess() throws Exception {
        mockMvc.perform(post("/setting/profile")
                        .param("introduce","test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/setting/profile"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("프로필 변경 - 실패")
    @WithUserDetails(value="jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateProfileFailure() throws Exception {
        mockMvc.perform(post("/setting/profile")
                        .param("introduce","12345678910111213141516")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().hasErrors());
    }


    @Test
    @DisplayName("로그인 하지 않고 회원탈퇴 요청")
    void unauthenticatedSignOut() throws Exception {
        mockMvc.perform(get("/setting/sign-out"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("회원 탈퇴")
    @WithUserDetails(value="jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void signOut() throws Exception {
        mockMvc.perform(post("/setting/sign-out")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Member member = memberRepository.findByNickname("jun");
        assertNull(member);
    }

    @Test
    @DisplayName("인증되지 않은 비밀번호 변경 화면 접근")
    void unauthenticatedUpdatePasswordPage() throws Exception {
        mockMvc.perform(get("/setting/password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("비밀번호 변경 화면")
    @WithUserDetails(value="jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePasswordPage() throws Exception{
        mockMvc.perform(get("/setting/password"))
                .andExpect(status().isOk())
                .andExpect(view().name("setting/password"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    @WithUserDetails(value="jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePasswordSuccess() throws Exception{
        mockMvc.perform(post("/setting/password")
                        .param("password","123456789")
                        .param("passwordConfirm","123456789")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/setting/password"))
                .andExpect(flash().attributeExists("message"));

        Member member = memberRepository.findByNickname("jun");
        assertTrue(passwordEncoder.matches("123456789",member.getPassword()));
    }

    @Test
    @DisplayName("비밀번호 변경 - 실패")
    @WithUserDetails(value="jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePasswordFailure() throws Exception {
        //비밀번호 불일치
        mockMvc.perform(post("/setting/password")
                        .param("password","123456789")
                        .param("passwordConfirm","123444444")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("setting/password"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().hasErrors());

        Member member = memberRepository.findByNickname("jun");
        assertFalse(passwordEncoder.matches("123456789",member.getPassword()));

        //비밀번호 최소 범위
        mockMvc.perform(post("/setting/password")
                        .param("password","1234")
                        .param("passwordConfirm","1234")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("setting/password"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().hasErrors());

        member = memberRepository.findByNickname("jun");
        assertFalse(passwordEncoder.matches("1234",member.getPassword()));

        //비밀번호 최대 범위
        mockMvc.perform(post("/setting/password")
                        .param("password","123456789101112131415")
                        .param("passwordConfirm","123456789101112131415")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("setting/password"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().hasErrors());

        member = memberRepository.findByNickname("jun");
        assertFalse(passwordEncoder.matches("123456789101112131415",member.getPassword()));

        //존재하지 않는 계정의 비밀번호를 변경하려 했을때
        memberRepository.deleteAll();
        mockMvc.perform(post("/setting/password")
                        .param("password","123123123")
                        .param("passwordConfirm","123123123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/setting/password"))
                .andExpect(flash().attribute("message","비밀번호 수정 중 문제가 발생했습니다. 다시 시도해주세요."));
    }
}