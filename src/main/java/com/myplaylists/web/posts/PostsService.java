package com.myplaylists.web.posts;

import com.myplaylists.domain.*;
import com.myplaylists.web.member.MemberRepository;
import com.myplaylists.web.posts.form.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostsService {

    private final PostsRepository postsRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;

    public Optional<Posts> getPosts(String postsId) {
        return postsRepository.findByIdUsingFetchJoin(Long.parseLong(postsId));
    }

    public Page<Posts> getAllPosts(Pageable pageable) {
        return postsRepository.findAllUsingFetchJoin(pageable);
    }

    public Page<Posts> searchPosts(String keyword,Pageable pageable) {
        return postsRepository.findByTitleContaining(keyword,pageable);
    }

    @Transactional
    public Posts addPosts(PostsForm postsForm, Member member) {
        List<PlaylistsForm> playlistsForms = postsForm.getPlaylistsForms();
        List<Playlist> playlists = new ArrayList<>();

        for(PlaylistsForm playlistsForm : playlistsForms){
            List<Link> links = new ArrayList<>();

            //link 리스트 만들기
            playlistsForm.getLinks().forEach(l -> links.add(Link.createLink(l)));

            //playlist 리스트 만들기
            Playlist playlist = Playlist.createPlaylist(playlistsForm.getPlaylistTitle(),
                    playlistsForm.getPlaylistDescription(),
                    playlistsForm.getPlaylistType(),
                    links);

            playlists.add(playlist);
        }

        Member byNickname = memberRepository.findByNickname(member.getNickname());

        Posts posts = Posts.createPosts(byNickname, postsForm.getTitle(),
                postsForm.getDescription(), playlists);

        Posts newPosts = postsRepository.save(posts);

        return newPosts;
    }

    @Transactional
    public boolean deletePosts(Member member,String postsId) {
        Optional<Posts> posts = getPosts(postsId);

        if(posts.isEmpty()){
            return false;
        }

        Member postsOwner = memberRepository.findByNickname(member.getNickname());

        if(!posts.get().getPostsOwner().equals(postsOwner)){
            return false;
        }

        postsRepository.delete(posts.get());

        return true;
    }

    @Transactional
    public boolean updatePosts(Posts posts, PostsEditForm postsEditForm){
        posts.setTitle(postsEditForm.getTitle());
        posts.setDescription(postsEditForm.getDescription());
        posts.setModifiedAt(LocalDateTime.now());

        Map<Long,Playlist> playlists = posts.getPlaylists()
                .stream()
                .collect(Collectors.toMap(Playlist::getId, playlist -> playlist));
        List<Playlist> updatePlaylists = new ArrayList<>();

        for(PlaylistsEditForm playlistsEditForm :postsEditForm.getPlaylistsEditForms()){
            Long curId = playlistsEditForm.getId();
            //ID가 빈칸
            if(curId == null){
                throw new RuntimeException("롤백");
            }
            //새로운 Playlist
            else if(curId == -1L){
                List<Link> links = new ArrayList<>();

                //link 리스트 만들기
                playlistsEditForm.getLinks().forEach(l -> links.add(Link.createLink(l)));

                //Playlist 리스트 만들기
                Playlist newPlaylist = Playlist.createPlaylist(playlistsEditForm.getTitle(),
                        playlistsEditForm.getDescription(),
                        playlistsEditForm.getPlaylistType(),
                        links);

                newPlaylist.setPosts(posts);
                updatePlaylists.add(newPlaylist);
            }
            //기존 Playlist 수정
            else if(playlists.containsKey(curId)){
                Playlist prevPlaylist = playlists.get(curId);

                prevPlaylist.setPlaylistType(playlistsEditForm.getPlaylistType());
                prevPlaylist.setTitle(playlistsEditForm.getTitle());
                prevPlaylist.setDescription(playlistsEditForm.getDescription());

                List<Link> prevLinks = prevPlaylist.getLinks();
                List<Link> tmpLinks = playlistsEditForm.getLinks().stream()
                        .map(l -> Link.createLink(l, prevPlaylist)).collect(Collectors.toList());

                for(int i = 0; i < tmpLinks.size(); i++){
                    if(i < prevLinks.size()){
                        prevLinks.get(i).setLink(tmpLinks.get(i).getLink());
                    }
                    else{
                        prevPlaylist.addLink(tmpLinks.get(i));
                    }
                }
                for(int i = prevLinks.size() - 1; i >= tmpLinks.size(); i--){
                    prevLinks.get(i).setPlaylist(null);
                    prevLinks.remove(i);
                }
                updatePlaylists.add(prevPlaylist);
            }
            //새로운 Playlist 가 아니면서 현재 Posts 에 소속되지 않은 Playlist
            else if(!playlists.containsKey(curId)){
                throw new RuntimeException("롤백");
            }
        }
        posts.getPlaylists().clear();
        posts.getPlaylists().addAll(updatePlaylists);

        postsRepository.save(posts);

        return true;
    }

    public Optional<Comment> getComment(String commentId) {
        return commentRepository.findById(Long.parseLong(commentId));
    }

    @Transactional
    public void addComment(Member member, Posts posts, String comment) {
        Member byNickname = memberRepository.findByNickname(member.getNickname());
        Comment.createComment(byNickname, posts, comment);
    }

    @Transactional
    public void removeComment(Comment comment) {
        commentRepository.delete(comment);
    }

    public List<Comment> getAllComment(Posts commentedPosts) {
        Optional<List<Comment>> byCommentedPosts = commentRepository.findByCommentedPosts(commentedPosts);
        return byCommentedPosts.get();
    }

    @Transactional
    public void updateComment(Comment comment, CommentForm commentForm) {
        comment.setContent(commentForm.getContent());
        comment.setModifiedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }
}
