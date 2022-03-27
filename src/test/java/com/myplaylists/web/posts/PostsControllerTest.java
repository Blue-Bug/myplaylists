package com.myplaylists.web.posts;

import com.myplaylists.domain.Link;
import com.myplaylists.domain.Member;
import com.myplaylists.domain.Playlist;
import com.myplaylists.domain.Posts;
import com.myplaylists.web.member.MemberRepository;
import com.myplaylists.web.member.MemberService;
import com.myplaylists.web.member.form.SignUpForm;
import com.myplaylists.web.posts.form.PlaylistsEditForm;
import com.myplaylists.web.posts.form.PostsEditForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.parameters.P;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
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
    ModelMapper modelMapper;
    @Autowired
    EntityManager em;

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
    void postsDelete_WithOutLogin() throws Exception {
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK1");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.save(posts);

        String deleteUrl = "/posts/"+posts.getId()+"/remove";

        mockMvc.perform(post(deleteUrl)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.isPresent());
    }


    @Test
    @DisplayName("Posts 삭제 - 실패(소유자가 아닌 유저의 요청)")
    @WithUserDetails(value = "jun1",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void postsDelete_NotOwned() throws Exception {
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK1");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.save(posts);

        String deleteUrl = "/posts/"+posts.getId()+"/remove";

        mockMvc.perform(post(deleteUrl)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"))
                .andExpect(redirectedUrl("/"));

        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        assertTrue(byPostsOwner.isPresent());
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

        //2번째 페이지에서 posts 가 받아져 오는지 확인
        mockMvc.perform(get("/").param("page","1"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(status().isOk());

        //3번째 페이지에서 posts 요소가 있는지 확인
        mockMvc.perform(get("/").param("page","2"))
                .andExpect(model().attributeDoesNotExist("posts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Posts 수정 화면")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editPostsPage() throws Exception{
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK1");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.saveAndFlush(posts);

        String editUrl = "/posts/"+posts.getId()+"/edit";

        mockMvc.perform(get(editUrl))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postsEditForm"))
                .andExpect(view().name("posts/edit"));
    }

    @Test
    @DisplayName("로그인 없이 Posts 수정 화면 접근")
    void editPostsPage_NoLogin() throws Exception {
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK1");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.saveAndFlush(posts);

        String editUrl = "/posts/"+posts.getId()+"/edit";

        mockMvc.perform(get(editUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("소유자가 아닌 유저가 Posts 수정 화면 접근")
    @WithUserDetails(value = "jun1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editPostsPage_NotOwned() throws Exception{
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK1");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.saveAndFlush(posts);

        String editUrl = "/posts/"+posts.getId()+"/edit";

        mockMvc.perform(get(editUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error","게시물의 소유자가 아닙니다."))
                .andExpect(redirectedUrl("/posts/"+posts.getId()));
    }

    @Test
    @DisplayName("존재하지 않는 Posts 수정 화면 접근")
    @WithUserDetails(value = "jun1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editPostsPage_NotExists() throws Exception{
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK1");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.saveAndFlush(posts);

        String editUrl = "/posts/"+posts.getId()+1+"/edit";

        mockMvc.perform(get(editUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error","존재하지 않는 게시물입니다."))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Posts 수정 - 성공")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editPosts_Success() throws Exception {
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.saveAndFlush(posts);

        //수정할 url
        String editUrl = "/posts/"+posts.getId()+"/edit";

        //수정할 내용
        PlaylistsEditForm playlistsEditForm1 = new PlaylistsEditForm();
        playlistsEditForm1.setId(posts.getPlaylists().get(0).getId());
        playlistsEditForm1.setTitle("EDIT PL TITLE1");
        playlistsEditForm1.setDescription("EDIT PL DESCRIPTION1");
        playlistsEditForm1.setPlaylistType("C");
        playlistsEditForm1.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PlaylistsEditForm playlistsEditForm2 = new PlaylistsEditForm();
        playlistsEditForm2.setId(-1L);
        playlistsEditForm2.setTitle("EDIT PL TITLE2");
        playlistsEditForm2.setDescription("EDIT PL DESCRIPTION2");
        playlistsEditForm2.setPlaylistType("Y");
        playlistsEditForm2.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PostsEditForm postsEditForm = new PostsEditForm();
        postsEditForm.setTitle("EDIT TEST");
        postsEditForm.setDescription("EDIT TESTING...");
        postsEditForm.setPlaylistsEditForms(List.of(playlistsEditForm1,playlistsEditForm2));

        //posts 수정
        mockMvc.perform(post(editUrl)
                        .with(csrf())
                        .param("title",postsEditForm.getTitle())
                        .param("description",postsEditForm.getDescription())
                        .param("playlistsEditForm.id",playlistsEditForm1.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm1.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm1.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm1.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm1.getLinks().size()))
                        .param("playlistsEditForm.id",playlistsEditForm2.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm2.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm2.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm2.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm2.getLinks().size()))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(2))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(2)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/"+posts.getId()));


        //수정 내용 확인
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        Posts comparePosts = byPostsOwner.get().get(0);
        Posts editedPosts = modelMapper.map(postsEditForm, Posts.class);
        for(int i = 0; i < postsEditForm.getPlaylistsEditForms().size(); i++){
            editedPosts.getPlaylists().get(i).setLinks(postsEditForm.getPlaylistsEditForms().get(i)
                    .getLinks().stream().map(l -> Link.createLink(l)).collect(Collectors.toList()));
        }

        assertEquals(editedPosts.getTitle(),comparePosts.getTitle());
        assertEquals(editedPosts.getDescription(),comparePosts.getDescription());
        assertEquals(editedPosts.getPlaylists().size(),comparePosts.getPlaylists().size());

        for(int i = 0; i < comparePosts.getPlaylists().size(); i++){
            assertEquals(editedPosts.getPlaylists().get(i).getTitle()
                    ,comparePosts.getPlaylists().get(i).getTitle());

            assertEquals(editedPosts.getPlaylists().get(i).getDescription()
                    ,comparePosts.getPlaylists().get(i).getDescription());

            assertEquals(editedPosts.getPlaylists().get(i).getPlaylistType()
                    ,comparePosts.getPlaylists().get(i).getPlaylistType());

            assertEquals(editedPosts.getPlaylists().get(i).getLinks().size()
                    ,comparePosts.getPlaylists().get(i).getLinks().size());

            for(int j = 0; j < comparePosts.getPlaylists().get(i).getLinks().size(); j++){
                assertEquals(editedPosts.getPlaylists().get(i).getLinks().get(i).getLink()
                        ,comparePosts.getPlaylists().get(i).getLinks().get(i).getLink());
            }
        }
    }

    @Test
    @DisplayName("Posts 수정 - 실패(소유자가 아닌 유저의 요청)")
    @WithUserDetails(value = "jun1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editPosts_NotOwned() throws Exception{
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.saveAndFlush(posts);

        //수정할 url
        String editUrl = "/posts/"+posts.getId()+"/edit";

        //수정할 내용
        PlaylistsEditForm playlistsEditForm1 = new PlaylistsEditForm();
        playlistsEditForm1.setId(posts.getPlaylists().get(0).getId());
        playlistsEditForm1.setTitle("EDIT PL TITLE1");
        playlistsEditForm1.setDescription("EDIT PL DESCRIPTION1");
        playlistsEditForm1.setPlaylistType("C");
        playlistsEditForm1.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PlaylistsEditForm playlistsEditForm2 = new PlaylistsEditForm();
        playlistsEditForm2.setId(-1L);
        playlistsEditForm2.setTitle("EDIT PL TITLE2");
        playlistsEditForm2.setDescription("EDIT PL DESCRIPTION2");
        playlistsEditForm2.setPlaylistType("Y");
        playlistsEditForm2.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PostsEditForm postsEditForm = new PostsEditForm();
        postsEditForm.setTitle("EDIT TEST");
        postsEditForm.setDescription("EDIT TESTING...");
        postsEditForm.setPlaylistsEditForms(List.of(playlistsEditForm1,playlistsEditForm2));

        //posts 수정
        mockMvc.perform(post(editUrl)
                        .with(csrf())
                        .param("title",postsEditForm.getTitle())
                        .param("description",postsEditForm.getDescription())
                        .param("playlistsEditForm.id",playlistsEditForm1.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm1.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm1.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm1.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm1.getLinks().size()))
                        .param("playlistsEditForm.id",playlistsEditForm2.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm2.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm2.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm2.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm2.getLinks().size()))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(2))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(2)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error","게시물의 소유자가 아닙니다."))
                .andExpect(redirectedUrl("/posts/"+posts.getId()));

        //수정 내용 확인
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        Posts comparePosts = byPostsOwner.get().get(0);
        Posts editedPosts = modelMapper.map(postsEditForm, Posts.class);
        for(int i = 0; i < postsEditForm.getPlaylistsEditForms().size(); i++){
            editedPosts.getPlaylists().get(i).setLinks(postsEditForm.getPlaylistsEditForms().get(i)
                    .getLinks().stream().map(l -> Link.createLink(l)).collect(Collectors.toList()));
        }

        assertNotEquals(editedPosts.getTitle(),comparePosts.getTitle());
        assertNotEquals(editedPosts.getDescription(),comparePosts.getDescription());
        assertNotEquals(editedPosts.getPlaylists().size(),comparePosts.getPlaylists().size());

        for(int i = 0; i < comparePosts.getPlaylists().size(); i++){
            assertNotEquals(editedPosts.getPlaylists().get(i).getTitle()
                    ,comparePosts.getPlaylists().get(i).getTitle());

            assertNotEquals(editedPosts.getPlaylists().get(i).getDescription()
                    ,comparePosts.getPlaylists().get(i).getDescription());

            assertNotEquals(editedPosts.getPlaylists().get(i).getPlaylistType()
                    ,comparePosts.getPlaylists().get(i).getPlaylistType());

            assertNotEquals(editedPosts.getPlaylists().get(i).getLinks().size()
                    ,comparePosts.getPlaylists().get(i).getLinks().size());

            for(int j = 0; j < comparePosts.getPlaylists().get(i).getLinks().size(); j++){
                assertNotEquals(editedPosts.getPlaylists().get(i).getLinks().get(i).getLink()
                        ,comparePosts.getPlaylists().get(i).getLinks().get(i).getLink());
            }
        }
    }

    @Test
    @DisplayName("Posts 수정 - 실패(로그인 없이 요청)")
    void editPosts_NotLogin() throws Exception{
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.saveAndFlush(posts);

        //수정할 url
        String editUrl = "/posts/"+posts.getId()+"/edit";

        //수정할 내용
        PlaylistsEditForm playlistsEditForm1 = new PlaylistsEditForm();
        playlistsEditForm1.setId(posts.getPlaylists().get(0).getId());
        playlistsEditForm1.setTitle("EDIT PL TITLE1");
        playlistsEditForm1.setDescription("EDIT PL DESCRIPTION1");
        playlistsEditForm1.setPlaylistType("C");
        playlistsEditForm1.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PlaylistsEditForm playlistsEditForm2 = new PlaylistsEditForm();
        playlistsEditForm2.setId(-1L);
        playlistsEditForm2.setTitle("EDIT PL TITLE2");
        playlistsEditForm2.setDescription("EDIT PL DESCRIPTION2");
        playlistsEditForm2.setPlaylistType("Y");
        playlistsEditForm2.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PostsEditForm postsEditForm = new PostsEditForm();
        postsEditForm.setTitle("EDIT TEST");
        postsEditForm.setDescription("EDIT TESTING...");
        postsEditForm.setPlaylistsEditForms(List.of(playlistsEditForm1,playlistsEditForm2));

        //posts 수정
        mockMvc.perform(post(editUrl)
                        .with(csrf())
                        .param("title",postsEditForm.getTitle())
                        .param("description",postsEditForm.getDescription())
                        .param("playlistsEditForm.id",playlistsEditForm1.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm1.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm1.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm1.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm1.getLinks().size()))
                        .param("playlistsEditForm.id",playlistsEditForm2.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm2.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm2.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm2.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm2.getLinks().size()))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(2))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(2)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        //수정 내용 확인
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        Posts comparePosts = byPostsOwner.get().get(0);
        Posts editedPosts = modelMapper.map(postsEditForm, Posts.class);
        for(int i = 0; i < postsEditForm.getPlaylistsEditForms().size(); i++){
            editedPosts.getPlaylists().get(i).setLinks(postsEditForm.getPlaylistsEditForms().get(i)
                    .getLinks().stream().map(l -> Link.createLink(l)).collect(Collectors.toList()));
        }

        assertNotEquals(editedPosts.getTitle(),comparePosts.getTitle());
        assertNotEquals(editedPosts.getDescription(),comparePosts.getDescription());
        assertNotEquals(editedPosts.getPlaylists().size(),comparePosts.getPlaylists().size());

        for(int i = 0; i < comparePosts.getPlaylists().size(); i++){
            assertNotEquals(editedPosts.getPlaylists().get(i).getTitle()
                    ,comparePosts.getPlaylists().get(i).getTitle());

            assertNotEquals(editedPosts.getPlaylists().get(i).getDescription()
                    ,comparePosts.getPlaylists().get(i).getDescription());

            assertNotEquals(editedPosts.getPlaylists().get(i).getPlaylistType()
                    ,comparePosts.getPlaylists().get(i).getPlaylistType());

            assertNotEquals(editedPosts.getPlaylists().get(i).getLinks().size()
                    ,comparePosts.getPlaylists().get(i).getLinks().size());

            for(int j = 0; j < comparePosts.getPlaylists().get(i).getLinks().size(); j++){
                assertNotEquals(editedPosts.getPlaylists().get(i).getLinks().get(i).getLink()
                        ,comparePosts.getPlaylists().get(i).getLinks().get(i).getLink());
            }
        }
    }

    @Test
    @DisplayName("Posts 수정 - 실패(존재하지 않는 Posts 수정)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editPosts_NotExists_Posts() throws Exception{
        //수정할 url
        String editUrl = "/posts/"+2+"/edit";

        //수정할 내용
        PlaylistsEditForm playlistsEditForm1 = new PlaylistsEditForm();
        playlistsEditForm1.setId(21L);
        playlistsEditForm1.setTitle("EDIT PL TITLE1");
        playlistsEditForm1.setDescription("EDIT PL DESCRIPTION1");
        playlistsEditForm1.setPlaylistType("C");
        playlistsEditForm1.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PlaylistsEditForm playlistsEditForm2 = new PlaylistsEditForm();
        playlistsEditForm2.setId(-1L);
        playlistsEditForm2.setTitle("EDIT PL TITLE2");
        playlistsEditForm2.setDescription("EDIT PL DESCRIPTION2");
        playlistsEditForm2.setPlaylistType("Y");
        playlistsEditForm2.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PostsEditForm postsEditForm = new PostsEditForm();
        postsEditForm.setTitle("EDIT TEST");
        postsEditForm.setDescription("EDIT TESTING...");
        postsEditForm.setPlaylistsEditForms(List.of(playlistsEditForm1,playlistsEditForm2));

        //posts 수정
        mockMvc.perform(post(editUrl)
                        .with(csrf())
                        .param("title",postsEditForm.getTitle())
                        .param("description",postsEditForm.getDescription())
                        .param("playlistsEditForm.id",playlistsEditForm1.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm1.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm1.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm1.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm1.getLinks().size()))
                        .param("playlistsEditForm.id",playlistsEditForm2.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm2.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm2.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm2.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm2.getLinks().size()))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(2))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(2)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error","존재하지 않는 게시물입니다."))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Posts 수정 - 실패(존재하지 않는 Playlist 수정)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editPosts_NotExists_Playlist() throws Exception{
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.saveAndFlush(posts);

        //수정할 url
        String editUrl = "/posts/"+posts.getId()+"/edit";

        //수정할 내용
        PlaylistsEditForm playlistsEditForm1 = new PlaylistsEditForm();
        playlistsEditForm1.setId(posts.getPlaylists().get(0).getId());
        playlistsEditForm1.setTitle("EDIT PL TITLE1");
        playlistsEditForm1.setDescription("EDIT PL DESCRIPTION1");
        playlistsEditForm1.setPlaylistType("C");
        playlistsEditForm1.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PlaylistsEditForm playlistsEditForm2 = new PlaylistsEditForm();
        playlistsEditForm2.setId(-11L);
        playlistsEditForm2.setTitle("EDIT PL TITLE2");
        playlistsEditForm2.setDescription("EDIT PL DESCRIPTION2");
        playlistsEditForm2.setPlaylistType("Y");
        playlistsEditForm2.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PostsEditForm postsEditForm = new PostsEditForm();
        postsEditForm.setTitle("EDIT TEST");
        postsEditForm.setDescription("EDIT TESTING...");
        postsEditForm.setPlaylistsEditForms(List.of(playlistsEditForm1,playlistsEditForm2));

        //posts 수정
        mockMvc.perform(post(editUrl)
                        .with(csrf())
                        .param("title",postsEditForm.getTitle())
                        .param("description",postsEditForm.getDescription())
                        .param("playlistsEditForm.id",playlistsEditForm1.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm1.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm1.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm1.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm1.getLinks().size()))
                        .param("playlistsEditForm.id",playlistsEditForm2.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm2.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm2.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm2.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm2.getLinks().size()))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(2))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(2)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error","잘못된 요청입니다."))
                .andExpect(redirectedUrl("/posts/"+posts.getId()));

        em.clear();

        //수정 내용 확인
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        Posts comparePosts = byPostsOwner.get().get(0);
        Posts editedPosts = modelMapper.map(postsEditForm, Posts.class);
        for(int i = 0; i < postsEditForm.getPlaylistsEditForms().size(); i++){
            editedPosts.getPlaylists().get(i).setLinks(postsEditForm.getPlaylistsEditForms().get(i)
                    .getLinks().stream().map(l -> Link.createLink(l)).collect(Collectors.toList()));
        }

        assertNotEquals(editedPosts.getTitle(),comparePosts.getTitle());
        assertNotEquals(editedPosts.getDescription(),comparePosts.getDescription());
        assertNotEquals(editedPosts.getPlaylists().size(),comparePosts.getPlaylists().size());

        for(int i = 0; i < comparePosts.getPlaylists().size(); i++){
            assertNotEquals(editedPosts.getPlaylists().get(i).getTitle()
                    ,comparePosts.getPlaylists().get(i).getTitle());

            assertNotEquals(editedPosts.getPlaylists().get(i).getDescription()
                    ,comparePosts.getPlaylists().get(i).getDescription());

            assertNotEquals(editedPosts.getPlaylists().get(i).getPlaylistType()
                    ,comparePosts.getPlaylists().get(i).getPlaylistType());

            assertNotEquals(editedPosts.getPlaylists().get(i).getLinks().size()
                    ,comparePosts.getPlaylists().get(i).getLinks().size());

            for(int j = 0; j < comparePosts.getPlaylists().get(i).getLinks().size(); j++){
                assertNotEquals(editedPosts.getPlaylists().get(i).getLinks().get(i).getLink()
                        ,comparePosts.getPlaylists().get(i).getLinks().get(i).getLink());
            }
        }
    }

    @Test
    @DisplayName("Posts 수정 - 실패(Validation)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editPosts_Invalid() throws Exception{
        Member postsOwner = memberRepository.findByNickname("jun");
        Link link = Link.createLink("TEST LINK");
        Playlist playlist = Playlist.createPlaylist("TEST PLAYLIST"
                ,"TEST PLAYLIST DESCRIPTION"
                ,"Y"
                , List.of(link));
        Posts posts = Posts.createPosts(postsOwner
                ,"TEST"
                ,"TESTING..."
                ,List.of(playlist));

        postsRepository.saveAndFlush(posts);

        //수정할 url
        String editUrl = "/posts/"+posts.getId()+"/edit";

        //수정할 내용
        PlaylistsEditForm playlistsEditForm1 = new PlaylistsEditForm();
        playlistsEditForm1.setId(21L);
        playlistsEditForm1.setTitle("EDIT PL TITLE1OVERLENGTHOVERLENGTHOVERLENGTHOVERLENGTH");
        playlistsEditForm1.setDescription("EDIT PL DESCRIPTION1OVERLENGTHOVERLENGTHOVERLENGTH");
        playlistsEditForm1.setPlaylistType("");
        playlistsEditForm1.setLinks(List.of("TEST LINK1OVERLENGTHOVERLENGTHOVERLENGTHOVERLENGTHOVERLENGTH"
                ,"TEST LINK2","TEST LINk3"));

        PlaylistsEditForm playlistsEditForm2 = new PlaylistsEditForm();
        playlistsEditForm2.setId(-1L);
        playlistsEditForm2.setTitle("EDIT PL TITLE2");
        playlistsEditForm2.setDescription("EDIT PL DESCRIPTION2");
        playlistsEditForm2.setPlaylistType("Y");
        playlistsEditForm2.setLinks(List.of("TEST LINK1","TEST LINK2","TEST LINk3"));

        PostsEditForm postsEditForm = new PostsEditForm();
        postsEditForm.setTitle("EDIT TEST OVERLENGTHOVERLENGTHOVERLENGTH");
        postsEditForm.setDescription("EDIT TESTING...OVERLENGTHOVERLENGTHOVERLENGTHOVERLENGTH" +
                "OVERLENGTHOVERLENGTHOVERLENGTHOVERLENGTH");
        postsEditForm.setPlaylistsEditForms(List.of(playlistsEditForm1,playlistsEditForm2));

        //posts 수정
        mockMvc.perform(post(editUrl)
                        .with(csrf())
                        .param("title",postsEditForm.getTitle())
                        .param("description",postsEditForm.getDescription())
                        .param("playlistsEditForm.id",playlistsEditForm1.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm1.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm1.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm1.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm1.getLinks().size()))
                        .param("playlistsEditForm.id",playlistsEditForm2.getId().toString())
                        .param("playlistsEditForm.title",playlistsEditForm2.getTitle())
                        .param("playlistsEditForm.description",playlistsEditForm2.getDescription())
                        .param("playlistsEditForm.playlistType", playlistsEditForm2.getPlaylistType())
                        .param("playlistsEditForm.link_cnt", String.valueOf(playlistsEditForm2.getLinks().size()))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm1.getLinks().get(2))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(0))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(1))
                        .param("playlistsEditForm.links", playlistsEditForm2.getLinks().get(2)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postsId"))
                .andExpect(model().attributeExists("postsEditForm"))
                .andExpect(model().attributeExists("org.springframework.validation.BindingResult.postsEditForm"))
                .andExpect(model().attributeErrorCount("postsEditForm",6))
                .andExpect(view().name("posts/edit"));

        //수정 내용 확인
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(postsOwner);
        Posts comparePosts = byPostsOwner.get().get(0);
        Posts editedPosts = modelMapper.map(postsEditForm, Posts.class);
        for(int i = 0; i < postsEditForm.getPlaylistsEditForms().size(); i++){
            editedPosts.getPlaylists().get(i).setLinks(postsEditForm.getPlaylistsEditForms().get(i)
                    .getLinks().stream().map(l -> Link.createLink(l)).collect(Collectors.toList()));
        }

        assertNotEquals(editedPosts.getTitle(),comparePosts.getTitle());
        assertNotEquals(editedPosts.getDescription(),comparePosts.getDescription());
        assertNotEquals(editedPosts.getPlaylists().size(),comparePosts.getPlaylists().size());

        for(int i = 0; i < comparePosts.getPlaylists().size(); i++){
            assertNotEquals(editedPosts.getPlaylists().get(i).getTitle()
                    ,comparePosts.getPlaylists().get(i).getTitle());

            assertNotEquals(editedPosts.getPlaylists().get(i).getDescription()
                    ,comparePosts.getPlaylists().get(i).getDescription());

            assertNotEquals(editedPosts.getPlaylists().get(i).getPlaylistType()
                    ,comparePosts.getPlaylists().get(i).getPlaylistType());

            assertNotEquals(editedPosts.getPlaylists().get(i).getLinks().size()
                    ,comparePosts.getPlaylists().get(i).getLinks().size());

            for(int j = 0; j < comparePosts.getPlaylists().get(i).getLinks().size(); j++){
                assertNotEquals(editedPosts.getPlaylists().get(i).getLinks().get(i).getLink()
                        ,comparePosts.getPlaylists().get(i).getLinks().get(i).getLink());
            }
        }
    }
}