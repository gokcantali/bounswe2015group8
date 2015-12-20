package controller;

import com.cloudinary.utils.ObjectUtils;
import dao.MemberDaoImpl;
import model.*;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import service.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by gokcan on 25.10.2015.
 */

@Controller
public class MainController {
    @Autowired
    private MailSender mailSender;
    private SecureRandom random = new SecureRandom();

    MemberDetailsService memberService;
    PostService postService;
    HeritageService heritageService;

    @Autowired
    private ApplicationContext appContext;
    private Logger logger = Logger.getLogger(MainController.class);

    CommentService commentService;
    VoteService voteService;
    TagService tagService;
    FollowService followService;
    FollowHeritageService followHeritageService;

    public MainController() {
        memberService = new MemberDetailsService();
        MemberDaoImpl mdao = new MemberDaoImpl();
        mdao.setSessionFactory(Main.getSessionFactory());
        memberService.setMemberDao(mdao);
        postService = new PostService(Main.getSessionFactory());
        heritageService = new HeritageService(Main.getSessionFactory());
        commentService = new CommentService(Main.getSessionFactory());
        voteService = new VoteService(Main.getSessionFactory());
        tagService = new TagService(Main.getSessionFactory());
        followService = new FollowService(Main.getSessionFactory());
        followHeritageService = new FollowHeritageService(Main.getSessionFactory());
    }

    /// The controller for home page
    /**
     *
     * no parameter for this method
     * @return home page
     */

    @RequestMapping(value = "/")
    public ModelAndView home() {
        return new ModelAndView("home");
    }

    /// The controller for login
    /**
     *
     * @param username: username of the registered user (String)
     * @param password: password of registered user (String)
     * @return login_success jsp.
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView login(@RequestParam(value = "username") String username,
                              @RequestParam(value = "password") String password) {

        final Session session = Main.getSession();
        int numberUsers = 0;
        try {

            Criteria criteria = session.createCriteria(Member.class)
                    .add(Restrictions.eq("username", "xascsanlcs"))
                    .add(Restrictions.eq("password", password));
            numberUsers = criteria.list().size();

        } finally {
            session.close();
            if (numberUsers == 0) {
                return new ModelAndView("login", "doesUserExist", false);
            } else {
                return new ModelAndView("login_success", "username", username); // login_success.jsp will be created soon.
            }
        }


    }
/// The controller for forget password
    /**
     * Sends reset password mail to user
     * @param username: username of the registered user(String)
     * @redirect login page
     */
    @RequestMapping(value = "/forget_password", method = RequestMethod.POST)
    public ModelAndView forget_password(HttpServletRequest request,
                                        @RequestParam(value = "username") String username) {
        Member member = memberService.getMemberByUsername(username);
        if (member == null) {
            return new ModelAndView("redirect:/forget_password?userNotExists");
        }
        String email = member.getEmail();
        String baseURL = request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/"));
        logger.info("Base URL: " + baseURL);


        String token = new BigInteger(130, random).toString(32);
        String text = "Hello " + username + "! " + " We heard that you wanted to reset your password...\n\n";
        text += "You can click this link to reset your password: ";
        text += baseURL + "/reset_password?token=" + token;
        text += "\n Have a nice day...";

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("WeStory");
        mail.setTo(email);
        mail.setSubject("WeStory Password Reset");
        mail.setText(text);
        mailSender.send(mail);

        java.util.Date now = new java.util.Date();

        final Session session = Main.getSession();
        session.getTransaction().begin();
        ResetPassword rp = new ResetPassword(member, token, new Timestamp(now.getTime()));
        session.save(rp);
        session.getTransaction().commit();
        session.close();


        return new ModelAndView("redirect:/login?resetPassword=true");
    }
/// The controller for forget password page
    /**
     * When the username is wrong
     * @param notExists: Concludes value and required fields(String)
     * @return  forget password
     */
    @RequestMapping(value = "/forget_password", method = RequestMethod.GET)
    public ModelAndView forget_password_page(@RequestParam(value = "userNotExists", required = false) String notExists) {
        return new ModelAndView("forget_password");
    }

    @RequestMapping(value = "/reset_password", method = RequestMethod.GET)
    public ModelAndView reset_password_page(@RequestParam(value = "token") String token) {
        final Session session = Main.getSession();
        session.getTransaction().begin();
        ResetPassword rp = (ResetPassword) session.createCriteria(ResetPassword.class)
                .add(Restrictions.eq("token", token)).uniqueResult();
        Member member = rp.getMember();
        return new ModelAndView("reset_password", "username", member.getUsername());
    }

    @RequestMapping(value = "/reset_password", method = RequestMethod.POST)
    public ModelAndView reset_password(@RequestParam(value = "username") String username,
                                       @RequestParam(value = "password") String password) {
        logger.info("username " + username);
        logger.info("password " + password);
        Member member = memberService.getMemberByUsername(username);
        memberService.updatePassword(username, password);
        return new ModelAndView("redirect:/login?passwordChanged=true");
    }
/// The controller for login
    /**
     * Login
     * @return login
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    /// The controller for login success
    /**
     * Login success
     * @return login_success/username
     */
    @RequestMapping(value = "/login_success", method = RequestMethod.GET)
    public ModelAndView login_success() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        return new ModelAndView("login_success", "username", name);
    }
/// The controller for signup
    /**
     * Login
     * @param username: username of the new user(string)
     * @param password: password of the new user(string)
     * @param email: email of the new user(string)
     * @return login
     */
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ModelAndView signup(@RequestParam(value = "username") String username,
                               @RequestParam(value = "password") String password,
                               @RequestParam(value = "email") String email) {
        Member m = memberService.createMember(username, password, email, "");
        Map<String, String> templateVars = new HashMap<String, String>();
        templateVars.put("member_id", "" + m.getId());
        templateVars.put("username", m.getUsername());
        return new ModelAndView("login");
    }
/// The controller for signup
    /**
     * Signup
     * @return signup
     */
    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public ModelAndView signup() {
        return new ModelAndView("signup");
    }

    /// The controller for post
    /**
     * Post method
     * @param heritageId: heritage id of the related post(long)
     * @return post,viewVariables
     */
    @RequestMapping(value = "/post/{heritageId}")
    public ModelAndView post(@PathVariable long heritageId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Map viewVariables = new HashMap();
        viewVariables.put("username", username);
        viewVariables.put("heritageId", heritageId);
        return new ModelAndView("post", viewVariables);
    }
/// The controller for upload post
    /**
     * Upload post method
     * @param title: Title of the post(String)
     * @param content: Content of the post(String)
     * @param place: Place of the post(String)
     * @param media: Media content of the post, it can be audio, image or video
     * @param heritageId: Heritage id of the post's heritage (long)
     * @redirect show_posts/heritageId
     */
    @RequestMapping(value = "/upload_post", method = RequestMethod.POST)
    public ModelAndView upload_post(@RequestParam("title") String title,
                                    @RequestParam("content") String content,
                                    @RequestParam("place") String place,
                                    @RequestParam("media") MultipartFile media,
                                    @RequestParam("heritageId") long heritageId) {
        logger.info("heritage id: " + Long.toString(heritageId));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        /*
        final Session session = Main.getSession();
        session.getTransaction().begin();
        Member m = memberService.getMemberByUsername(username);

        java.util.Date now = new java.util.Date();

        Post post = new Post(m, 0, new Timestamp(now.getTime()), title, content);
        session.save(post);
        */
        java.util.Date now = new java.util.Date();
        Member m = memberService.getMemberByUsername(username);
        Heritage heritage = heritageService.getHeritageById(heritageId);
        Post post = postService.savePost(m, 0, new Timestamp(now.getTime()), title, content, place, heritage);

        if (!media.isEmpty()) {
            try {
                // Creating the directory to store file
                String mediaName = media.getOriginalFilename();
                String filePath = mediaName;

                File toUpload = new File(mediaName);
                toUpload.createNewFile();
                FileOutputStream fos = new FileOutputStream(toUpload);
                fos.write(media.getBytes());
                fos.close();

                final Session session = Main.getSession();
                session.getTransaction().begin();
                try {
                    Map utilsMap = ObjectUtils.asMap("resource_type", "auto");
                    Map uploadResult = CloudinaryController.getCloudinary().uploader().upload(toUpload, utilsMap);
                    int mediaType = CloudinaryController.getMediaType(mediaName);
                    logger.info("media type is " + mediaType);
                    Media mediaObject = new Media(post.getId(), uploadResult.get("url").toString(), mediaType, false);
                    session.save(mediaObject);
                    session.getTransaction().commit();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    return new ModelAndView("list_post", "error", "File uploaded failed:" + mediaName);
                }
            } catch (Exception e) {
                return new ModelAndView("list_post", "error", "You failed to upload the file" + e.getMessage());
            }
        }

        /*
        Criteria criteria = session.createCriteria(Post.class)
                .add(Restrictions.eq("owner", m));
        List posts = criteria.list();
        session.close();
        */

        List<Post> posts = postService.getPostsByMember(m);
        return new ModelAndView("redirect:/show_posts/" + heritageId);
    }
/// The controller for show posts
    /**
     * Upload show post method
     * @return list_post/allContent
     */
    @RequestMapping(value = "/show_posts")
    public ModelAndView show_posts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        final Session session = Main.getSession();

        logger.info("The current user is: " + username);
        Member m = memberService.getMemberByUsername(username);
        List posts = postService.getPostsByMember(m);

        List medias = session.createCriteria(Media.class).list();
        //List comments = session.createCriteria(Comment.class).list();
        Map<String, List> allContent = new HashMap<String, List>();
        allContent.put("posts", posts);
        allContent.put("medias", medias);
        //allContent.put("comments", comments);
        session.close();

        return new ModelAndView("list_post", "allContent", allContent);
    }
/// The controller for show_posts_of_heritage
    /**
     * Show posts of heritages
     * @param heritageId: Heritage id of the post's heritage (long)
     * @return list_post/allContent
     */
    @RequestMapping(value = "/show_posts/{heritageId}")
    public ModelAndView show_posts_of_heritage(@PathVariable long heritageId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        final Session session = Main.getSession();

        logger.info("The current user is: " + username);
        Member m = memberService.getMemberByUsername(username);
        Heritage heritage = heritageService.getHeritageById(heritageId);
        List posts = postService.getPostsByHeritage(heritage);
        List medias = session.createCriteria(Media.class).list();
        List allTags = session.createCriteria(Tag.class).list();
        List heritages = new ArrayList<Heritage>();
        logger.info("Current heritage: " + heritage.getName());
        heritages.add(heritage);
        logger.info("size of heritages: " + heritages.size());
        Map<String, List> allContent = new HashMap<String, List>();
        allContent.put("posts", posts);
        allContent.put("medias", medias);
        allContent.put("heritages", heritages);
        allContent.put("allTags", allTags);
        session.close();

        return new ModelAndView("list_post", "allContent", allContent);
    }
/// The controller for heritage
    /**
     * @return heritage
     */
    @RequestMapping(value = "/heritage")
    public ModelAndView heritage() {
        return new ModelAndView("heritage");
    }

    /// The controller for upload heritage
    /**
     * Upload post method
     * @param name: Name of the heritage(String)
     * @param place: Place of the heritage(String)
     * @param media: Media content of the heritage, it can be audio, image or video
     * @param description: Description of the heritage(String)
     * @redirect show_heritages
     */
    @RequestMapping(value = "/upload_heritage", method = RequestMethod.POST)
    public ModelAndView upload_heritage(@RequestParam("name") String name,
                                        @RequestParam("place") String place,
                                        @RequestParam("media") MultipartFile media,
                                        @RequestParam("description") String description) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        java.util.Date now = new java.util.Date();
        Heritage heritage = heritageService.saveHeritage(name, place, description, new Timestamp(now.getTime()));

        if (!media.isEmpty()) {
            try {
                // Creating the directory to store file
                String mediaName = media.getOriginalFilename();
                String filePath = mediaName;

                File toUpload = new File(mediaName);
                toUpload.createNewFile();
                FileOutputStream fos = new FileOutputStream(toUpload);
                fos.write(media.getBytes());
                fos.close();

                final Session session = Main.getSession();
                session.getTransaction().begin();

                try {
                    Map utilsMap = ObjectUtils.asMap("resource_type", "auto");
                    Map uploadResult = CloudinaryController.getCloudinary().uploader().upload(toUpload, utilsMap);
                    int mediaType = CloudinaryController.getMediaType(mediaName);
                    Media mediaObject = new Media(heritage.getId(), uploadResult.get("url").toString(), mediaType, true);
                    logger.info("WHAT IS IT? " + uploadResult.get("url").toString());
                    session.save(mediaObject);
                    session.getTransaction().commit();
                }  catch (RuntimeException e) {
                    e.printStackTrace();
                    return new ModelAndView("list_post", "error", "File uploaded failed:" + mediaName);
                }
            } catch (Exception e) {
                return new ModelAndView("list_post", "error", "You failed to upload the file" + e.getMessage());
            }
        }

        List<Heritage> allHeritages = heritageService.getAllHeritages();
        return new ModelAndView("redirect:/show_heritages");
    }
/// The controller for edit post
    /**
     * Upload post method
     * @param postId: Id of the post (long)
     * @return edit_post_page, viewVariables
     */
    @RequestMapping(value = "/edit_post/{postId}")
    public ModelAndView edit_post(@PathVariable long postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Post post = postService.getPostById(postId);
        String title = post.getTitle();
        String content = post.getContent();
        String place = post.getPlace();
        Map viewVariables = new HashMap();
        viewVariables.put("username", username);
        viewVariables.put("postId", postId);
        viewVariables.put("place", place);
        viewVariables.put("title", title);
        viewVariables.put("content", content);
        return new ModelAndView("edit_post_page", viewVariables);
    }
/// The controller for follow
    /**
     * Follow method
     * @param followeeId: Id of the followee(long)
     * if already follows
     * @return -1
     * if does not follow
     * @return 1
     */
    @RequestMapping(value = "/follow/{followeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public long follow(@PathVariable long followeeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Member m = memberService.getMemberByUsername(username);
        Member other = memberService.getMemberById(followeeId);
        if(followService.doesFollow(m, other)){
            return -1; // If already follows
        }
        followService.saveFollow(m.getId(), followeeId);
        return 1;
    }
/// The controller for unfollow
    /**
     * Unfollow method
     * @param followeeId: Id of followee(long)
     * if already does not follow
     * @return -1
     * if follows
     * @return 1
     */
    @RequestMapping(value = "/unfollow/{followeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public long unfollow(@PathVariable long followeeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Member m = memberService.getMemberByUsername(username);
        Member other = memberService.getMemberById(followeeId);
        if(!followService.doesFollow(m, other)){
            return -1; // If already does not follow
        }
        followService.deleteFollow(m, memberService.getMemberById(followeeId));
        return 1;
    }
/// The controller for update_post
    /**
     * Update post method
     * @param title: Title of the post(String)
     * @param content: Content of the post(String)
     * @param place: Place of the post(String)
     * @param media: Media content of the post, it can be audio, image or video
     * @redirect show_posts/heritageId
     */
    @RequestMapping(value = "/update_post", method = RequestMethod.POST)
    public ModelAndView update_post(@RequestParam("title") String title,
                                    @RequestParam("content") String content,
                                    @RequestParam("place") String place,
                                    @RequestParam("media") MultipartFile media,
                                    @RequestParam("postId") long postId ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        java.util.Date now = new java.util.Date();
        Member m = memberService.getMemberByUsername(username);
        Post post = postService.getPostById(postId);
        Heritage heritage = heritageService.getFirstHeritageByPost(post);
        long heritageId = heritage.getId();
        postService.updatePost(postId, title, content, place, new Timestamp(now.getTime()));
        logger.info("PLACCCCCEEEEE " + place);

        if (!media.isEmpty()) {
            try {
                // Creating the directory to store file
                String mediaName = media.getOriginalFilename();
                String filePath = mediaName;

                File toUpload = new File(mediaName);
                toUpload.createNewFile();
                FileOutputStream fos = new FileOutputStream(toUpload);
                fos.write(media.getBytes());
                fos.close();

                final Session session = Main.getSession();
                session.getTransaction().begin();
                try {
                    Map utilsMap = ObjectUtils.asMap("resource_type", "auto");
                    Map uploadResult = CloudinaryController.getCloudinary().uploader().upload(toUpload, utilsMap);
                    int mediaType = CloudinaryController.getMediaType(mediaName);
                    logger.info("media type is " + mediaType);
                    Media mediaObject = new Media(heritage.getId(), uploadResult.get("url").toString(), mediaType, true);
                    session.save(mediaObject);
                    session.getTransaction().commit();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    return new ModelAndView("list_post", "error", "File uploaded failed:" + mediaName);
                }
            } catch (Exception e) {
                return new ModelAndView("list_post", "error", "You failed to upload the file" + e.getMessage());
            }
        }

        return new ModelAndView("redirect:/show_posts/" + heritageId);
    }
/// The controller for show_heritages
    /**
     * Show heritages method
     * @return list_heritage/allContent
     */
    @RequestMapping(value = "/show_heritages")
    public ModelAndView show_heritages() {
        final Session session = Main.getSession();
        List<Heritage> allHeritages = heritageService.getAllHeritages();
        List medias = session.createCriteria(Media.class).list();
        List allTags = session.createCriteria(Tag.class).list();
        Map<String, List> allContent = new HashMap<String, List>();
        allContent.put("heritages", allHeritages);
        allContent.put("medias", medias);
        allContent.put("allTags", allTags);
        session.close();
        return new ModelAndView("list_heritage", "allContent", allContent);
    }
/// The controller for comment
    /**
     * Upload post method
     * @param postId: Id of the post (long)
     * @return comment,viewVariables
     */
    @RequestMapping(value = "/comment/{postId}")
    public ModelAndView comment(@PathVariable long postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Map viewVariables = new HashMap();
        viewVariables.put("username", username);
        viewVariables.put("postId", postId);
        return new ModelAndView("comment", viewVariables);
    }
/// The controller for upload_comment
    /**
     * Upload post method
     * @param content: Content of the post(String)
     * @param postId: Id of the post (long)
     * @return list_posts, viewVariables
     */
    @RequestMapping(value = "/post_comment", method = RequestMethod.POST)
    public ModelAndView upload_comment(@RequestParam("content") String content, @RequestParam("postId") long postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        final Session session = Main.getSession();
        session.getTransaction().begin();

        Member m = memberService.getMemberByUsername(username);
        Post post = postService.getPostById(postId);
        java.util.Date now = new java.util.Date();

        Comment comment = commentService.saveComment(m, post, content, new Timestamp(now.getTime()));

        Heritage heritage = heritageService.getFirstHeritageByPost(post);
        List heritages = new ArrayList<Heritage>();
        heritages.add(heritage);

        List posts = postService.getPostsByHeritage(heritage);

        List medias = session.createCriteria(Media.class).list();
        //List comments = session.createCriteria(Comment.class).list();
        Map<String, List> allContent = new HashMap<String, List>();
        allContent.put("posts", posts);
        allContent.put("medias", medias);
        allContent.put("heritages", heritages);
        //allContent.put("comments", comments);
        session.close();

        return new ModelAndView("list_post", "allContent", allContent);
    }

    // This function will be called via an AJAX request.
    // It returns the overall vote for that post. (upvotes - downvotes)
    /**
     * Vote post method
     * @param postId: Id of the post(long)
     * @param voteType : Vote type, it can increment or decrement 1 overall vote(boolean)
     * @return VoteService.getPostOverallVote(post)
     */
    @RequestMapping(value = "/vote_post/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public long vote_post(@PathVariable long postId,
                          @RequestParam(value = "voteType") boolean voteType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Member member = memberService.getMemberByUsername(username);
        Post post = postService.getPostById(postId);
        voteService.savePostVote(member, post, voteType);

        return voteService.getPostOverallVote(post);
    }

    // This function will be called via an AJAX request.
    // It returns the overall vote for that comment. (upvotes - downvotes)
    /**
     * Vote comment method
     * @param commentId: Id of the comment(long)
     * @param voteType : Vote type, it can increment or decrement 1 overall vote(boolean)
     * @return voteService.getCommentOverallVote(comment)
     */
    @RequestMapping(value = "/vote_comment/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public long vote_comment(@PathVariable long commentId,
                             @RequestParam(value = "voteType") boolean voteType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Member member = memberService.getMemberByUsername(username);
        Comment comment = commentService.getCommentById(commentId);
        voteService.saveCommentVote(member, comment, voteType);

        return voteService.getCommentOverallVote(comment);
    }
/// The controller for tag_heritage
    /**
     * Tag heritage method
     * @param heritageId: Id of the heritage (long)
     * @param tagTexts : Tag content of the heritage (String)
     * @return tags
     * */
    @RequestMapping(value = "/tag_heritage/{heritageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String[] tag_heritage(@PathVariable long heritageId,
                                 @RequestParam(value = "tagTexts[]") String[] tagTexts) {

        Heritage heritage = heritageService.getHeritageById(heritageId);
        for (int i = 0; i < tagTexts.length; i++) {
            tagService.addTag(tagTexts[i], heritage);
        }
        List<Tag> heritageTags = tagService.getTagsByHeritage(heritage);
        String[] tags = new String[heritageTags.size()];
        final Session session = Main.getSession();
        for (int i = 0; i < tags.length; i++) {
            session.update(heritageTags.get(i));
            tags[i] = heritageTags.get(i).getTagText();
            String context = heritageTags.get(i).getTagContext();
            if(context != null){
                tags[i] = tags[i] + "(" + context + ")";
            }
        }
        session.close();
        return tags;
    }
/// The controller for tag post
    /**
     * Tag post method
     * @param postId: Id of the post (long)
     * @param tagTexts : Tag content of the posts (String)
     * @return tags
     */
    @RequestMapping(value = "/tag_post/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String[] tag_post(@PathVariable long postId,
                             @RequestParam(value = "tagTexts[]") String[] tagTexts) {


        Post post = postService.getPostById(postId);
        for (int i = 0; i < tagTexts.length; i++) {
            tagService.addTag(tagTexts[i], post);
        }
        List<Tag> postTags = tagService.getTagsByPost(post);
        String[] tags = new String[postTags.size()];
        final Session session = Main.getSession();
        for (int i = 0; i < tags.length; i++) {
            session.update(postTags.get(i));
            tags[i] = postTags.get(i).getTagText();
            String context = postTags.get(i).getTagContext();
            if(context != null){
                tags[i] = tags[i] + "(" + context + ")";
            }
        }
        session.close();
        return tags;
    }

    @RequestMapping(value = "/google_map")
    public ModelAndView googleMap(){
        return new ModelAndView("google_map");
    }

    /// The controller for link post
    /**
     * Link post method
     * @param heritageName: Name of the heritage (String)
     * @param postId: Id of the post (long)
     * if there is no heritage with name as heritageName param
     * @return -2
     * if there is a heritage with name heritageName but no posts with id as postId
     * @return -1
     * if link is done
     * @return 1
     */
    @RequestMapping(value = "/linkPostWithHeritage/{heritageName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public long linkPost(@PathVariable String heritageName,
                         @RequestParam(value = "postId") long postId){

        if(heritageService.getHeritageByName(heritageName) == null){
            return -2;
        }
        Heritage heritage = heritageService.getHeritageByName(heritageName);
        if(heritageService.doesHeritageHavePost(heritage.getId(), postService.getPostById(postId))){
            return -1;
        }
        postService.linkPostWithHeritage(postId, heritage);
        return 1;
    }
/// The controller for link post page
    /**
     * Link post page method
     * @param postId: Id of the post (long)
     * @return link_post/allContent
     */
    @RequestMapping(value = "/linkPostWithHeritage/{postId}", method = RequestMethod.GET)
    public ModelAndView linkPostPage(@PathVariable long postId){
        List<Post> posts = new ArrayList<>();
        posts.add(postService.getPostById(postId));
        Map<String, List> allContent = new HashMap<String, List>();
        allContent.put("posts", posts);
        allContent.put("heritages", heritageService.getAllHeritages());
        return new ModelAndView("link_post", "allContent", allContent);
    }
/// The controller for signup
    /**
     * Upload post method
     * @param heritageId: Id of the heritage(long)
     * @return 1
     */
    @RequestMapping(value = "/followHeritage/{heritageId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public long followHeritage(@PathVariable long heritageId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        long memberId = memberService.getMemberByUsername(username).getId();
        FollowHeritage fh = followHeritageService.saveFollowHeritage(memberId, heritageId);
        if(fh == null)
            return -1;
        return 1;
    }

    @RequestMapping("/profile")
    public ModelAndView profile(){
        return new ModelAndView("under_construction");
    }


}
