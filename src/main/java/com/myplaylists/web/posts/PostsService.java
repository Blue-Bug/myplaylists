package com.myplaylists.web.posts;

import com.myplaylists.domain.Member;
import com.myplaylists.domain.Playlist;
import com.myplaylists.domain.Posts;
import com.myplaylists.domain.Link;
import com.myplaylists.web.member.MemberRepository;
import com.myplaylists.web.posts.form.PlaylistsForm;
import com.myplaylists.web.posts.form.PostsForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostsService {

    private final PostsRepository postsRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

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

        return postsRepository.save(posts);
    }

    @Transactional(readOnly = true)
    public Optional<Posts> getPosts(String postsId) {
        return postsRepository.findByIdUsingFetchJoin(Long.parseLong(postsId));
    }

    @Transactional(readOnly = true)
    public List<Posts> getAllPosts() {
        return postsRepository.findAllUsingFetchJoin();
    }

    public boolean deletePosts(Member member,String postsId) {
        Optional<Posts> posts = getPosts(postsId);

        if(posts.isEmpty()) return false;
        Member postsOwner = memberRepository.findByNickname(member.getNickname());
        if(!posts.get().getPostsOwner().equals(postsOwner)) return false;

        postsRepository.delete(posts.get());
        return true;
    }
}
