package com.myplaylists.web.posts;

import com.myplaylists.domain.Link;
import com.myplaylists.domain.Member;
import com.myplaylists.domain.Posts;
import com.myplaylists.web.member.MemberRepository;
import com.myplaylists.web.member.MemberService;
import com.myplaylists.web.member.form.SignUpForm;
import com.myplaylists.web.posts.form.PlaylistsForm;
import com.myplaylists.web.posts.form.PostsForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostsControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberService memberService;
    @Autowired
    PostsRepository postsRepository;
    @Autowired
    PostsService postsService;

    @BeforeEach
    void BeforeEach(){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("jun");
        signUpForm.setEmail("test@test.com");
        signUpForm.setPassword("123123123");
        memberService.joinProcess(signUpForm);

        SignUpForm signUpForm1 = new SignUpForm();
        signUpForm1.setNickname("jun1");
        signUpForm1.setEmail("test1@test.com");
        signUpForm1.setPassword("123123123");
        memberService.joinProcess(signUpForm1);
    }

    @AfterEach
    void AfterEach(){
        postsRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("Posts 작성 화면")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPostsPage() throws Exception {
        mockMvc.perform(get("/posts/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"))
                .andExpect(model().attributeExists("postsForm"));
    }

    @Test
    @DisplayName("로그인 없이 Posts 작성 화면 접근")
    void createPostsPage_NoLogin() throws Exception {
        mockMvc.perform(get("/posts/create"))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Posts 작성 - 성공")
    @WithUserDetails(value="jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Success() throws Exception {

        mockMvc.perform(post("/posts/create")
                    .with(csrf())
                    .param("title","TEST")
                    .param("description","TESTING...")
                    .param("playlistsForms[0].playlistTitle","TEST PLAYLIST1")
                    .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                    .param("playlistsForms[0].playlistType","Y")
                    .param("playlistsForms[0].links","TEST LINK1")
                    .param("playlistsForms[0].links","TEST LINK2")
                    .param("playlistsForms[0].links","TEST LINK3")
                    .param("playlistsForms[1].playlistTitle","TEST PLAYLIST2")
                    .param("playlistsForms[1].playlistDescription","TEST PLAYLIST DESCRIPTION2")
                    .param("playlistsForms[1].playlistType","Y")
                    .param("playlistsForms[1].links","TEST LINK4")
                    .param("playlistsForms[1].links","TEST LINK5")
                    .param("playlistsForms[1].links","TEST LINK6"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/**"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);

        assertFalse(byPostsOwner.get().isEmpty());
        assertEquals("TEST",byPostsOwner.get().get(0).getTitle());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(로그인 없음)")
    void createPosts_WithOutLogin() throws Exception {
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST1")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1")
                        .param("playlistsForms[0].links","TEST LINK2")
                        .param("playlistsForms[0].links","TEST LINK3")
                        .param("playlistsForms[1].playlistTitle","TEST PLAYLIST2")
                        .param("playlistsForms[1].playlistDescription","TEST PLAYLIST DESCRIPTION2")
                        .param("playlistsForms[1].playlistType","Y")
                        .param("playlistsForms[1].links","TEST LINK4")
                        .param("playlistsForms[1].links","TEST LINK5")
                        .param("playlistsForms[1].links","TEST LINK6"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"))
                .andExpect(unauthenticated());

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);

        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(게시물 제목이 없음)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Posts_Has_NoTitle() throws Exception {
        /**
         * 로그인 되어있어야 한다.
         *
         * 게시물 제목 NotBlank,min=1,max=30
         * 게시물 설명 max=80
         * */

        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST1")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1")
                        .param("playlistsForms[0].links","TEST LINK2")
                        .param("playlistsForms[0].links","TEST LINK3")
                        .param("playlistsForms[1].playlistTitle","TEST PLAYLIST2")
                        .param("playlistsForms[1].playlistDescription","TEST PLAYLIST DESCRIPTION2")
                        .param("playlistsForms[1].playlistType","Y")
                        .param("playlistsForms[1].links","TEST LINK4")
                        .param("playlistsForms[1].links","TEST LINK5")
                        .param("playlistsForms[1].links","TEST LINK6"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(게시물 제목이 최대 길이 초과)")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Posts_Title_LengthOver() throws Exception {
        /**
        * 로그인 되어있어야 한다.
        *
        * 게시물 제목 NotBlank,min=1,max=30
        * 게시물 설명 max=80
        * */

        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TESTTESTTESTTESTTESTTESTTESTTESTTESTTEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST1")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1")
                        .param("playlistsForms[0].links","TEST LINK2")
                        .param("playlistsForms[0].links","TEST LINK3")
                        .param("playlistsForms[1].playlistTitle","TEST PLAYLIST2")
                        .param("playlistsForms[1].playlistDescription","TEST PLAYLIST DESCRIPTION2")
                        .param("playlistsForms[1].playlistType","Y")
                        .param("playlistsForms[1].links","TEST LINK4")
                        .param("playlistsForms[1].links","TEST LINK5")
                        .param("playlistsForms[1].links","TEST LINK6"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());

    }

    @Test
    @DisplayName("Posts 작성 - 실패(게시물 제목이 Blank)")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Posts_Title_Blank() throws Exception {
        /**
        * 로그인 되어있어야 한다.
        *
        * 게시물 제목 NotBlank,min=1,max=30
        * 게시물 설명 max=80
        * */

        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","   ")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST1")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1")
                        .param("playlistsForms[0].links","TEST LINK2")
                        .param("playlistsForms[0].links","TEST LINK3")
                        .param("playlistsForms[1].playlistTitle","TEST PLAYLIST2")
                        .param("playlistsForms[1].playlistDescription","TEST PLAYLIST DESCRIPTION2")
                        .param("playlistsForms[1].playlistType","Y")
                        .param("playlistsForms[1].links","TEST LINK4")
                        .param("playlistsForms[1].links","TEST LINK5")
                        .param("playlistsForms[1].links","TEST LINK6"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(게시물 설명이 최대 길이 초과)")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Posts_Description_LengthOver() throws Exception {
        /**
        * 로그인 되어있어야 한다.
        *
        * 게시물 제목 NotBlank,min=1,max=30
        * 게시물 설명 max=80
        * */

        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST1")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1")
                        .param("playlistsForms[0].links","TEST LINK2")
                        .param("playlistsForms[0].links","TEST LINK3")
                        .param("playlistsForms[1].playlistTitle","TEST PLAYLIST2")
                        .param("playlistsForms[1].playlistDescription","TEST PLAYLIST DESCRIPTION2")
                        .param("playlistsForms[1].playlistType","Y")
                        .param("playlistsForms[1].links","TEST LINK4")
                        .param("playlistsForms[1].links","TEST LINK5")
                        .param("playlistsForms[1].links","TEST LINK6"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(Playlist 가 없음)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Posts_Has_NoPlaylists() throws Exception {
        /**
         * 로그인 되어있어야 한다.
         *
         * 플레이리스트 List min=1,max=10
         *    플레이리스트 타입 NotBlank
         *    플레이리스트 제목 NotBlank,max=20
         *    플레이리스트 설명 max=30
         *    링크 List NotBlank,min=1,max=30
         * */
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(Playlist 최대 개수 초과)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Posts_Has_PlaylistsOver() throws Exception {
        /**
         * 로그인 되어있어야 한다.
         *
         * 플레이리스트 List min=1,max=10
         *    플레이리스트 타입 NotBlank
         *    플레이리스트 제목 NotBlank,max=20
         *    플레이리스트 설명 max=30
         *    링크 List NotBlank,min=1,max=30
         * */

        MockHttpServletRequestBuilder requestBuilder = post("/posts/create")
                .with(csrf())
                .param("title","TEST")
                .param("description","TESTING...");

        for(int i = 0; i < 11; i++){
            requestBuilder
                    .param("playlistsForms["+i+"].playlistTitle","TEST")
                    .param("playlistsForms["+i+"].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                    .param("playlistsForms["+i+"].playlistType","Y")
                    .param("playlistsForms["+i+"].links","TEST LINK1");
        }
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(Playlist 타입이 없음)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Playlists_Has_NoType() throws Exception {
        /**
         * 로그인 되어있어야 한다.
         *
         * 플레이리스트 List min=1,max=10
         *    플레이리스트 타입 NotBlank
         *    플레이리스트 제목 NotBlank,max=20
         *    플레이리스트 설명 max=30
         *    링크 List NotBlank,min=1,max=30
         * */
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","")
                        .param("playlistsForms[0].links","TEST LINK1")
                        .param("playlistsForms[0].links","TEST LINK2")
                        .param("playlistsForms[0].links","TEST LINK3")
                        .param("playlistsForms[1].playlistTitle","TEST PLAYLIST2")
                        .param("playlistsForms[1].playlistDescription","TEST PLAYLIST DESCRIPTION2")
                        .param("playlistsForms[1].playlistType","Y")
                        .param("playlistsForms[1].links","TEST LINK4")
                        .param("playlistsForms[1].links","TEST LINK5")
                        .param("playlistsForms[1].links","TEST LINK6"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(Playlist 제목이 없음)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Playlists_Has_NoTitle() throws Exception {
        /**
         * 로그인 되어있어야 한다.
         *
         * 플레이리스트 List min=1,max=10
         *    플레이리스트 타입 NotBlank
         *    플레이리스트 제목 NotBlank,max=20
         *    플레이리스트 설명 max=30
         *    링크 List NotBlank,min=1,max=30
         * */
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1")
                        .param("playlistsForms[0].links","TEST LINK2")
                        .param("playlistsForms[0].links","TEST LINK3")
                        .param("playlistsForms[1].playlistTitle","TEST PLAYLIST2")
                        .param("playlistsForms[1].playlistDescription","TEST PLAYLIST DESCRIPTION2")
                        .param("playlistsForms[1].playlistType","Y")
                        .param("playlistsForms[1].links","TEST LINK4")
                        .param("playlistsForms[1].links","TEST LINK5")
                        .param("playlistsForms[1].links","TEST LINK6"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(Playlist 제목이 최대 길이 초과)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Playlists_Title_LengthOver() throws Exception {
        /**
         * 로그인 되어있어야 한다.
         *
         * 플레이리스트 List min=1,max=10
         *    플레이리스트 타입 NotBlank
         *    플레이리스트 제목 NotBlank,max=20
         *    플레이리스트 설명 max=30
         *    링크 List NotBlank,min=1,max=30
         * */
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(Playlist 설명이 최대 길이 초과)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Playlists_Description_LengthOver() throws Exception {
        /**
         * 로그인 되어있어야 한다.
         *
         * 플레이리스트 List min=1,max=10
         *    플레이리스트 타입 NotBlank
         *    플레이리스트 제목 NotBlank,max=20
         *    플레이리스트 설명 max=30
         *    링크 List NotBlank,min=1,max=30
         * */
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST TITLE")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(Playlist 링크가 Blank)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Playlists_Link_Blank() throws Exception {
        /**
         * 로그인 되어있어야 한다.
         *
         * 플레이리스트 List min=1,max=10
         *    플레이리스트 타입 NotBlank
         *    플레이리스트 제목 NotBlank,max=20
         *    플레이리스트 설명 max=30
         *    링크 List NotBlank,min=1,max=30
         * */
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST TITLE")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links",""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(Playlist 링크가 없음)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Playlists_Has_NoLink() throws Exception {
        /**
         * 로그인 되어있어야 한다.
         *
         * 플레이리스트 List min=1,max=10
         *    플레이리스트 타입 NotBlank
         *    플레이리스트 제목 NotBlank,max=20
         *    플레이리스트 설명 max=30
         *    링크 List NotBlank,min=1,max=30
         * */
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST TITLE")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 작성 - 실패(Playlist 링크가 최대 개수 초과)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPosts_Playlists_Has_LinkOver() throws Exception {
        /**
         * 로그인 되어있어야 한다.
         *
         * 플레이리스트 List min=1,max=10
         *    플레이리스트 타입 NotBlank
         *    플레이리스트 제목 NotBlank,max=20
         *    플레이리스트 설명 max=30
         *    링크 List NotBlank,min=1,max=30
         * */
        MockHttpServletRequestBuilder requestBuilder = post("/posts/create")
                .with(csrf())
                .param("title", "TEST")
                .param("description", "TESTING...")
                .param("playlistsForms[0].playlistTitle", "TEST PLAYLIST TITLE")
                .param("playlistsForms[0].playlistDescription", "TEST PLAYLIST DESCRIPTION1")
                .param("playlistsForms[0].playlistType", "Y");

        for(int i = 0; i < 31; i++){
            requestBuilder.param("playlistsForms[0].links","TEST");
        }
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("posts/create"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.get().isEmpty());
    }

    @Test
    @DisplayName("Posts 삭제 - 성공")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void postsDelete_Success() throws Exception {
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST1")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1")
                        .param("playlistsForms[0].links","TEST LINK2")
                        .param("playlistsForms[0].links","TEST LINK3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/**"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);

        assertFalse(byPostsOwner.get().isEmpty());
        assertEquals("TEST",byPostsOwner.get().get(0).getTitle());

        String deleteUrl = "/posts/"+byPostsOwner.get().get(0).getId()+"/remove";
        mockMvc.perform(post(deleteUrl)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Posts 삭제 - 실패(존재하지 않는 Posts 삭제)")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void postsDelete_Failure() throws Exception {
        String deleteUrl = "/posts/2/remove";
        mockMvc.perform(post(deleteUrl)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Posts 삭제 - 실패(로그인 없음)")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void postsDelete_WithOutLogin() throws Exception {
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST1")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1")
                        .param("playlistsForms[0].links","TEST LINK2")
                        .param("playlistsForms[0].links","TEST LINK3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/**"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);

        assertFalse(byPostsOwner.get().isEmpty());
        assertEquals("TEST",byPostsOwner.get().get(0).getTitle());
        String deleteUrl = "/posts/"+byPostsOwner.get().get(0).getId()+"/remove";

        mockMvc.perform(post("/logout")
                .with(csrf()));

        mockMvc.perform(post(deleteUrl)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    @Test
    @DisplayName("Posts 삭제 - 실패(다른 유저의 요청)")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void postsDelete_NotOwned() throws Exception {
        mockMvc.perform(post("/posts/create")
                        .with(csrf())
                        .param("title","TEST")
                        .param("description","TESTING...")
                        .param("playlistsForms[0].playlistTitle","TEST PLAYLIST1")
                        .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                        .param("playlistsForms[0].playlistType","Y")
                        .param("playlistsForms[0].links","TEST LINK1")
                        .param("playlistsForms[0].links","TEST LINK2")
                        .param("playlistsForms[0].links","TEST LINK3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/**"));

        Member postsOwner = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);

        assertFalse(byPostsOwner.get().isEmpty());
        assertEquals("TEST",byPostsOwner.get().get(0).getTitle());
        String deleteUrl = "/posts/"+byPostsOwner.get().get(0).getId()+"/remove";

        mockMvc.perform(post("/logout")
                .with(csrf()));

        mockMvc.perform(post("/login")
                .with(csrf())
                .param("username","jun1")
                .param("password","123123123"));

        mockMvc.perform(post(deleteUrl)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    @Test
    @DisplayName("Posts Paging 확인")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void postsPaging() throws Exception{
        for(int i = 0; i < 12; i++){
            mockMvc.perform(post("/posts/create")
                            .with(csrf())
                            .param("title","TEST")
                            .param("description","TESTING...")
                            .param("playlistsForms[0].playlistTitle","TEST PLAYLIST1")
                            .param("playlistsForms[0].playlistDescription","TEST PLAYLIST DESCRIPTION1")
                            .param("playlistsForms[0].playlistType","Y")
                            .param("playlistsForms[0].links","TEST LINK1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/posts/**"));
        }

        //총 페이지가 2개인지 확인
        mockMvc.perform(get("/"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("totalPage",2))
                .andExpect(status().isOk());

        //2번째 페이지에서 posts가 받아져 오는지 확인
        mockMvc.perform(get("/").param("page","1"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(status().isOk());

        //3번째 페이지에서 posts 요소가 있는지 확인
        mockMvc.perform(get("/").param("page","2"))
                .andExpect(model().attributeDoesNotExist("posts"))
                .andExpect(status().isOk());
    }
}