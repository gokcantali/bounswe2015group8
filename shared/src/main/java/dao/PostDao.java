package dao;

import model.Heritage;
import model.Member;
import model.Post;
import model.Tag;
import org.hibernate.SessionFactory;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by gokcan on 08.11.2015.
 */
public interface PostDao {
    Post getPostById(long id);

    List<Post> getPostsByOwner(Member owner);

    List<Post> getPostsByHeritage(Heritage heritage);

    List<Post> getPostsByTag(Tag tag);

    List<Post> getPostsContainTitle(String title);

    List<Post> getPostsCreatedAfter(Timestamp date);

    List<Post> getPostsCreatedAfter(Timestamp date, Heritage heritage);

    Post savePost(Post post, Heritage heritage);

    Post updatePost(Post post);

    SessionFactory getSessionFactory();

    void setSessionFactory(SessionFactory sessionFactory);
}
