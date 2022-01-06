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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
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

        Posts posts = Posts.createPosts(member, postsForm.getTitle(),
                postsForm.getDescription(), playlists);

        //작성한 글 증가
        memberRepository.findByNickname(member.getNickname()).addWrittenPosts();

        return postsRepository.save(posts);
    }

    public Optional<Posts> getPosts(String postsId) {
        return postsRepository.findById(Long.parseLong(postsId));
    }
}
