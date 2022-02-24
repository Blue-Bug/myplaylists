package com.myplaylists.web.resolver;

import com.myplaylists.web.posts.form.PlaylistsEditForm;
import com.myplaylists.web.posts.form.PostsEditForm;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class PlaylistsArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(PostsEditForm.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest req = (HttpServletRequest) webRequest.getNativeRequest();

        PostsEditForm postsEditForm = new PostsEditForm();

        postsEditForm.setTitle(req.getParameter("title"));
        postsEditForm.setDescription(req.getParameter("description"));

        List<PlaylistsEditForm> playlistsEditForms = new ArrayList<>();

        int[] linkCnt = Arrays.stream(req.getParameterValues("playlistsEditForm.link_cnt")).mapToInt(i -> Integer.parseInt(i)).toArray();

        String[] titles = req.getParameterValues("playlistsEditForm.title");
        String[] descriptions = req.getParameterValues("playlistsEditForm.description");
        String[] types = req.getParameterValues("playlistsEditForm.playlistType");
        long[] ids = Arrays.stream(req.getParameterValues("playlistsEditForm.id")).mapToLong(i -> Long.parseLong(i)).toArray();
        String[] links = req.getParameterValues("playlistsEditForm.links");

        int linkIdx = 0;

        for(int i = 0; i < ids.length; i++){
            PlaylistsEditForm playlistsEditForm = new PlaylistsEditForm();
            playlistsEditForm.setId(ids[i]);
            playlistsEditForm.setPlaylistType(types[i]);
            playlistsEditForm.setTitle(titles[i]);
            playlistsEditForm.setDescription(descriptions[i]);
            List<String> tmp = new ArrayList<>();
            for(int j = linkIdx; j < linkIdx+linkCnt[i]; j++){
                tmp.add(links[j]);
            }
            linkIdx += linkCnt[i];
            playlistsEditForm.setLinks(tmp);

            playlistsEditForms.add(playlistsEditForm);
        }

        postsEditForm.setPlaylistsEditForms(playlistsEditForms);

        if(parameter.hasParameterAnnotation(Valid.class)){
            WebDataBinder binder = binderFactory.createBinder(webRequest, postsEditForm, "postsEditForm");
            binder.validate();
            Map<String, Object> bindingResultModel = binder.getBindingResult().getModel();
            mavContainer.removeAttributes(bindingResultModel);
            mavContainer.addAllAttributes(bindingResultModel);
        }

        return postsEditForm;
    }
}
