package controller;

import com.cloudinary.utils.ObjectUtils;
import dao.MemberDaoImpl;
import model.Media;
import model.Member;
import model.Tag;
import org.hibernate.Session;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import service.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by gokcan on 20.12.2015.
 */
@Controller
public class ProfileController {

    MemberDetailsService memberService;
    PostService postService;
    HeritageService heritageService;
    TagService tagService;
    FollowTagService followTagService;

    public ProfileController(){
        memberService = new MemberDetailsService();
        MemberDaoImpl mdao = new MemberDaoImpl();
        mdao.setSessionFactory(Main.getSessionFactory());
        memberService.setMemberDao(mdao);
        postService = new PostService(Main.getSessionFactory());
        heritageService = new HeritageService(Main.getSessionFactory());
        tagService = new TagService(Main.getSessionFactory());
        followTagService = new FollowTagService(Main.getSessionFactory());
    }

    @RequestMapping("/profile/{username}")
    public ModelAndView userProfile(@PathVariable String username){
        Member member = memberService.getMemberByUsername(username);
        return new ModelAndView("profile", "user", member);
    }

    @RequestMapping(value = "/uploadProfilePicture", method = RequestMethod.POST)
    public ModelAndView uploadProfilePicture(@RequestParam("picture") MultipartFile pictureMedia) throws Exception{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try{
            String mediaName = pictureMedia.getOriginalFilename();

            File toUpload = new File(mediaName);
            toUpload.createNewFile();
            FileOutputStream fos = new FileOutputStream(toUpload);
            fos.write(pictureMedia.getBytes());
            fos.close();

            try {
                Map utilsMap = ObjectUtils.asMap("resource_type", "auto");
                Map uploadResult = CloudinaryController.getCloudinary().uploader().upload(toUpload, utilsMap);
                memberService.updateProfilePicture(username, uploadResult.get("url").toString());
            } catch (RuntimeException e) {
                e.printStackTrace();
                return new ModelAndView("list_post", "error", "Cloudinary upload failed..");
            }
        }
        catch(IOException e){
            e.printStackTrace();
            return new ModelAndView("profile", "error", "The file could not be uploaded!");
        }
        return new ModelAndView("redirect:/profile/"+username);

    }

    @RequestMapping(value = "/followTag", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String[] followTag(@RequestParam("tags[]") String[] tags){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        long userId = memberService.getMemberByUsername(username).getId();
        String[] newTagsToFollow = new String[tags.length];

        for(int i = 0; i < tags.length; i++) {
            String tagText = tags[i];
            String tagContext = null;
            String[] tagPieces = tagService.extractTextAndContext(tagText);
            tagText = tagPieces[0];
            tagContext = tagPieces[1];
            Tag tag;
            if(tagService.doesTagExist(tagText, tagContext)){
                tag = tagService.getTagByText(tagText, tagContext);
            }
            else{
                tag = tagService.saveTag(tagText, tagContext);
            }
            followTagService.saveFollowTag(userId, tag.getId());
            newTagsToFollow[i] = tagText;
            if(tagContext != null)
                newTagsToFollow[i] += "(" + tagContext + ")";
        }
        return newTagsToFollow;
    }

}
