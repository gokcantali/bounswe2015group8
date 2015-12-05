package com.cmpe.bounswe2015group8.westory.back;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmpe.bounswe2015group8.westory.model.Comment;
import com.cmpe.bounswe2015group8.westory.model.Heritage;
import com.cmpe.bounswe2015group8.westory.model.Member;
import com.cmpe.bounswe2015group8.westory.model.Post;
import com.cmpe.bounswe2015group8.westory.model.Requestable;

import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Class handling all server requests through REST API.
 * Originally created by Bugrahan Memis, later changed by Doruk Kilitcioglu
 * to comply with REST API.
 * @author bugrahan
 * @author xyllan
 * Date: 31/10/15.
 */
public class ServerRequests{
    public static final String SERVER_ADDRESS = "http://ec2-54-187-115-133.us-west-2.compute.amazonaws.com:8080/lokum_v3";
    private ProgressDialog progressDialog;
    private boolean display;
    public ServerRequests(Context context){
        this(context, true);
    }
    public ServerRequests(Context context, boolean display) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing...");
        progressDialog.setMessage("Please wait...");
        this.display = display;

    }
    public void getPostById(long id, Consumer<Post> callback) {
        if(display) progressDialog.show();
        Map<String,String> m = new HashMap<>();
        m.put("id", Long.toString(id));
        new RestAsyncTask<Post>(callback, HttpMethod.POST).execute(new Requestable<Post>("/api/getPostById", m, Post.class));
    }
    public void getPostsByHeritageId(long id, Consumer<Post[]> callback) {
        if(display) progressDialog.show();
        new RestAsyncTask<Post[]>(callback, HttpMethod.POST).execute(new Requestable<Post[]>("/api/getHeritagePostsById", id, Post[].class));
    }
    public void getCommentsByPostId(long id, Consumer<Comment[]> callback) {
        progressDialog.show();
        new RestAsyncTask<Comment[]>(callback, HttpMethod.GET).execute(new Requestable<Comment[]>("/api/getCommentsByPostId/"+id, id, Comment[].class));
    }
    public void getAllPosts(Consumer<Post[]> callback) {
        if(display) progressDialog.show();
        new RestAsyncTask<Post[]>(callback, HttpMethod.POST).execute(new Requestable<Post[]>("/api/getAllPosts",null,Post[].class) );
    }
    public void getAllComments(Consumer<Comment[]> callback) {
        progressDialog.show();
        new RestAsyncTask<Comment[]>(callback, HttpMethod.GET).execute(new Requestable<Comment[]>("/api/getAllComments",null,Comment[].class) );
    }
    public void createPost(Post p, long heritageId, Consumer<Long> callback) {
        if(display) progressDialog.show();
        new RestAsyncTask<Long>(callback, HttpMethod.POST).execute(p.getCreateRequestable(heritageId));
    }
    public void createComment(Comment c, long postId, Consumer<String> callback){
        progressDialog.show();
        new RestAsyncTask<String>(callback, HttpMethod.POST).execute(c.getCreateRequestable(postId));
    }

    public void getHeritageById(long id, Consumer<Heritage> callback) {
        if(display) progressDialog.show();
        Map<String,String> m = new HashMap<>();
        m.put("id", Long.toString(id));
        new RestAsyncTask<Heritage>(callback, HttpMethod.POST).execute(new Requestable<Heritage>("/api/getHeritageById", m, Heritage.class));
    }
    public void getAllHeritages(Consumer<Heritage[]> callback) {
        if(display) progressDialog.show();
        new RestAsyncTask<Heritage[]>(callback, HttpMethod.POST).execute(new Requestable<Heritage[]>("/api/getAllHeritages",null,Heritage[].class) );
    }
    public void createHeritage(Heritage h, Consumer<Long> callback) {
        if(display) progressDialog.show();
        new RestAsyncTask<Long>(callback, HttpMethod.POST).execute(h.getCreateRequestable());
    }
    public void login(Member m, Consumer<Member> callback) {
        if(display) progressDialog.show();
        new RestAsyncTask<Member>(callback, HttpMethod.POST).execute(m.getLoginRequestable());
    }
    public void register(Member m, Consumer<Long> callback) {
        if(display) progressDialog.show();
        new RestAsyncTask<Long>(callback, HttpMethod.POST).execute(m.getRegisterRequestable());
    }
    public class RestAsyncTask<T> extends AsyncTask<Requestable<T>, Void, T> {
        Consumer<T> callback;
        HttpMethod method;
        public RestAsyncTask(Consumer<T>  callback, HttpMethod method) {
            this.callback = callback;
            this.method = method;
        }

        @Override
        protected T doInBackground(Requestable<T>... params) {
            RestTemplate rt = new RestTemplate(true);
            rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            switch (method) {
                case GET:
                    return rt.getForObject(SERVER_ADDRESS + params[0].getEndpoint(), params[0].getDataClass());
                case POST:
                    return rt.postForObject(SERVER_ADDRESS + params[0].getEndpoint(), params[0].getData(), params[0].getDataClass());
                default:
                    return null;
            }
        }

        @Override
        protected void onPostExecute(T t) {
            if(display) progressDialog.dismiss();
            callback.accept(t);
            super.onPostExecute(t);
        }
    }
}
