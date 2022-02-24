package com.myplaylists.web.posts;

import com.myplaylists.domain.Member;
import com.myplaylists.domain.Playlist;
import com.myplaylists.domain.Posts;
import com.myplaylists.domain.Link;
import com.myplaylists.web.member.MemberRepository;
import com.myplaylists.web.posts.LinkRepository;
import com.myplaylists.web.posts.PlaylistRepository;
import com.myplaylists.web.posts.PostsRepository;
import com.myplaylists.web.posts.form.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
    private final PlaylistRepository playlistRepository;
    private final LinkRepository linkRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public Posts addPosts(PostsForm postsForm, Member member) {
        List<PlaylistsForm> playlistsForms = postsForm.getPlaylistsForms();
        List<Playlist> playlists = new ArrayList<>();
        List<Link> allLinks = new ArrayList<>();

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
            allLinks.addAll(links);
        }

        Member byNickname = memberRepository.findByNickname(member.getNickname());

        Posts posts = Posts.createPosts(byNickname, postsForm.getTitle(),
                postsForm.getDescription(), playlists);

        Posts newPosts = postsRepository.saveAndFlush(posts);
        playlistRepository.saveAll(playlists);
        linkRepository.saveAll(allLinks);

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

        List<Playlist> playlists = posts.get().getPlaylists();
        List<Link> links = new ArrayList<>();

        for(Playlist playlist : playlists){
            links.addAll(playlist.getLinks());
        }

        linkRepository.deleteAllInBatch(links);
        playlistRepository.deleteAllInBatch(playlists);
        postsRepository.delete(posts.get());

        return true;
    }

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
    public boolean updatePosts(String postsId, PostsEditForm postsEditForm) {
        Posts posts = postsRepository.getById(Long.parseLong(postsId));
        posts.setTitle(postsEditForm.getTitle());
        posts.setDescription(postsEditForm.getDescription());
        posts.setModifiedAt(LocalDateTime.now());

        Map<Long,Playlist> playlists = posts.getPlaylists()
                .stream()
                .collect(Collectors.toMap(Playlist::getId, playlist -> playlist));

        List<Playlist> updatePlaylists = new ArrayList<>();
        List<Link> updateLinks = new ArrayList<>();

        List<Playlist> deletePlaylists = new ArrayList<>();
        List<Link> deleteLinks = new ArrayList<>();

        for(PlaylistsEditForm playlistsEditForm :postsEditForm.getPlaylistsEditForms()){
            Long curId = playlistsEditForm.getId();
            //ID가 빈칸
            if(curId == null){
                return false;
            }
            //새로운 Playlist
            else if(curId == -1L){
                List<Link> links = new ArrayList<>();

                //link 리스트 만들기
                playlistsEditForm.getLinks().forEach(l -> links.add(Link.createLink(l)));

                //Playlist 리스트 만들기
                Playlist playlist = Playlist.createPlaylist(playlistsEditForm.getTitle(),
                        playlistsEditForm.getDescription(),
                        playlistsEditForm.getPlaylistType(),
                        links);

                playlist.setPosts(posts);
                updatePlaylists.add(playlist);
                updateLinks.addAll(links);
            }
            //기존 Playlist 수정
            else if(playlists.containsKey(curId)){
                Playlist updatePlaylist = playlists.get(curId);

                updatePlaylist.setPlaylistType(playlistsEditForm.getPlaylistType());
                updatePlaylist.setTitle(playlistsEditForm.getTitle());
                updatePlaylist.setDescription(playlistsEditForm.getDescription());

                List<Link> prevLinks = updatePlaylist.getLinks();
                List<Link> tmpLinks = playlistsEditForm.getLinks().stream()
                        .map(l -> Link.createLink(l, updatePlaylist)).collect(Collectors.toList());


                for(int i = 0; i < tmpLinks.size(); i++){
                    if(i < prevLinks.size()){
                        prevLinks.get(i).setLink(tmpLinks.get(i).getLink());
                        updateLinks.add(prevLinks.get(i));
                    }
                    else{
                        updateLinks.add(tmpLinks.get(i));
                    }
                }
                for(int i = tmpLinks.size(); i < prevLinks.size(); i++){
                    deleteLinks.add(prevLinks.get(i));
                }

                updatePlaylists.add(updatePlaylist);
                playlists.remove(curId);
            }
            //새로운 Playlist가 아니면서 현재 Posts에 소속되지 않은 Playlist
            else if(!playlists.containsKey(curId)){
                return false;
            }
        }

        //남아있는 Playlist 는 지워야함
        if(!playlists.isEmpty()){
            deleteLinks.addAll(playlists.entrySet()
                    .stream()
                    .flatMap(p-> p.getValue().getLinks().stream())
                    .collect(Collectors.toList()));

            deletePlaylists.addAll(new ArrayList<>(playlists.values()));
        }

        linkRepository.deleteAllInBatch(deleteLinks);
        playlistRepository.deleteAllInBatch(deletePlaylists);

        linkRepository.saveAll(updateLinks);
        playlistRepository.saveAll(updatePlaylists);
        postsRepository.save(posts);

        return true;
    }
}
