package com.myplaylists.web.posts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myplaylists.domain.Comment;
import com.myplaylists.domain.Member;
import com.myplaylists.domain.Posts;
import com.myplaylists.web.member.MemberRepository;
import com.myplaylists.web.member.MemberService;
import com.myplaylists.web.member.form.SignUpForm;
import com.myplaylists.web.posts.form.CommentAddForm;
import com.myplaylists.web.posts.form.PlaylistsForm;
import com.myplaylists.web.posts.form.PostsForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostsService postsService;

    @Autowired
    PostsRepository postsRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CommentRepository commentRepository;

    @BeforeEach
    void beforeEach(){
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

        Member member = memberRepository.findByNickname("jun");
        Member member2 = memberRepository.findByNickname("jun1");

        PlaylistsForm playlistsForm = new PlaylistsForm();
        playlistsForm.setPlaylistTitle("TEST PLAYLIST");
        playlistsForm.setPlaylistDescription("TEST PLAYLIST DESCRIPTION");
        playlistsForm.setLinks(Arrays.asList("TEST LINK"));
        playlistsForm.setPlaylistType("Y");

        PostsForm postsForm = new PostsForm();
        postsForm.setTitle("TEST");
        postsForm.setDescription("TESTING...");
        postsForm.setPlaylistsForms(Arrays.asList(playlistsForm));
        postsService.addPosts(postsForm,member);
        postsService.addPosts(postsForm,member2);
    }

    @Test
    @DisplayName("댓글 조회 - 성공")
    void readComment_Success() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(member);
        Long postsId = byPostsOwner.get().get(0).getId();

        mockMvc.perform(get("/posts/"+postsId+"/comment").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("댓글 조회 - 실패")
    void readComment_Failure() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(member);
        Long postsId = byPostsOwner.get().get(0).getId();

        mockMvc.perform(get("/posts/"+postsId+1+"/comment").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"errors\":\"Posts가 존재하지 않습니다.\"}"));
    }

    @Test
    @DisplayName("댓글 작성 - 성공")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createComment_Success() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(member);
        Long postsId = byPostsOwner.get().get(0).getId();

        CommentAddForm commentAddForm = new CommentAddForm();
        commentAddForm.setContent("TEST");

        mockMvc.perform(post("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentAddForm))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        List<Comment> allComment = commentRepository.findAll();
        assertEquals(allComment.size(),1);
    }

    @Test
    @DisplayName("댓글 작성 - 실패(길이 초과)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createComment_Failure_LengthOver() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Optional<List<Posts>> byPostsOwner = postsRepository.findByPostsOwner(member);
        Long postsId = byPostsOwner.get().get(0).getId();

        CommentAddForm commentAddForm = new CommentAddForm();
        commentAddForm.setContent("TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST");

        mockMvc.perform(post("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentAddForm))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        List<Comment> allComment = commentRepository.findAll();
        assertTrue(allComment.isEmpty());
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteComment_Success() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(member).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);

        Long postsId = posts.getId();

        mockMvc.perform(delete("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+"\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        List<Comment> allComment = commentRepository.findAll();
        assertFalse(allComment.isEmpty());
    }

    @Test
    @DisplayName("댓글 삭제 - 실패(작성자가 다름)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteComment_Failure_NotOwned() throws Exception{
        Member member = memberRepository.findByNickname("jun1");
        Member postsOwner = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(postsOwner).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);

        Long postsId = posts.getId();

        mockMvc.perform(delete("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+"\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"errors\":\"자신이 작성한 Comment만 삭제 할 수 있습니다.\"}"));

        List<Comment> allComment = commentRepository.findAll();
        assertFalse(allComment.isEmpty());
    }

    @Test
    @DisplayName("댓글 삭제 - 실패(잘못된 URI)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteComment_Failure_WrongURI() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(member).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);

        Member member2 = memberRepository.findByNickname("jun1");
        Posts posts2 = postsRepository.findByPostsOwner(member2).get().get(0);
        Long postsId = posts2.getId();

        mockMvc.perform(delete("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+"\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"errors\":\"잘못된 요청입니다.\"}"));

        List<Comment> allComment = commentRepository.findAll();
        assertFalse(allComment.isEmpty());
    }

    @Test
    @DisplayName("댓글 삭제 - 실패(존재하지 않는 댓글)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteComment_Failure_NotExists() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(member).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);
        Long postsId = posts.getId();

        mockMvc.perform(delete("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+1+"\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"errors\":\"Comment가 존재하지 않습니다.\"}"));

        List<Comment> allComment = commentRepository.findAll();
        assertFalse(allComment.isEmpty());
    }

    @Test
    @DisplayName("댓글 삭제 - 실패(존재하지 않는 Posts)")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteComment_Failure_Posts_NotExists() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(member).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);
        Long postsId = posts.getId();

        mockMvc.perform(delete("/posts/"+postsId+1+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+"\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"errors\":\"Posts가 존재하지 않습니다.\"}"));

        List<Comment> allComment = commentRepository.findAll();
        assertFalse(allComment.isEmpty());
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    @WithUserDetails(value = "jun", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editComment_Success() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(member).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);
        Long postsId = posts.getId();

        mockMvc.perform(patch("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+"\", \"content\" : \"TEST EDITING\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));


        assertEquals("TEST EDITING",newComment.getContent());
    }

    @Test
    @DisplayName("댓글 수정 - 실패(작성자가 다름)")
    @WithUserDetails(value = "jun1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editComment_Failure_NotOwned() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(member).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);
        Long postsId = posts.getId();

        mockMvc.perform(patch("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+"\", \"content\" : \"TEST EDITING\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"errors\":\"자신이 작성한 Comment만 수정 할 수 있습니다.\"}"));

        assertEquals("TEST",newComment.getContent());
    }

    @Test
    @DisplayName("댓글 수정 - 실패(길이 초과)")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editComment_Failure_LengthOver() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(member).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);
        Long postsId = posts.getId();

        mockMvc.perform(patch("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+"\", \"content\" : \"TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"errors\":\"length must be between 1 and 50\"}"));

        assertEquals("TEST",newComment.getContent());
    }

    @Test
    @DisplayName("댓글 수정 - 실패(잘못된 URI)")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editComment_Failure_WrongURI() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(member).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);

        Member member2 = memberRepository.findByNickname("jun1");
        Posts posts2 = postsRepository.findByPostsOwner(member2).get().get(0);
        Long postsId = posts2.getId();

        mockMvc.perform(patch("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+"\", \"content\" : \"TEST EDITING\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"errors\":\"잘못된 요청입니다.\"}"));

        assertEquals("TEST",newComment.getContent());
    }

    @Test
    @DisplayName("댓글 수정 - 실패(존재하지 않는 댓글)")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editComment_Failure_NotExists() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(member).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);
        Long postsId = posts.getId();

        mockMvc.perform(patch("/posts/"+postsId+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+1+"\", \"content\" : \"TEST EDITING\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"errors\":\"Comment가 존재하지 않습니다.\"}"));

        assertEquals("TEST",newComment.getContent());
    }

    @Test
    @DisplayName("댓글 수정 - 실패(존재하지 않는 Posts)")
    @WithUserDetails(value = "jun",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editComment_Failure_Posts_NotExists() throws Exception {
        Member member = memberRepository.findByNickname("jun");
        Posts posts = postsRepository.findByPostsOwner(member).get().get(0);
        Comment newComment = Comment.createComment(member, posts, "TEST");
        commentRepository.saveAndFlush(newComment);
        Long postsId = posts.getId();

        mockMvc.perform(patch("/posts/"+postsId+1+"/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+newComment.getId()+"\", \"content\" : \"TEST EDITING\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"errors\":\"Posts가 존재하지 않습니다.\"}"));

        assertEquals("TEST",newComment.getContent());
    }

}
