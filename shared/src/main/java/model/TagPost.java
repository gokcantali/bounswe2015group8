package model;

/**
 * Created by xyllan on 22.10.2015.
 */
public class TagPost {
    private long tagId;
    private long postId;

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagPost tagPost = (TagPost) o;

        if (tagId != tagPost.tagId) return false;
        if (postId != tagPost.postId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (tagId ^ (tagId >>> 32));
        result = 31 * result + (int) (postId ^ (postId >>> 32));
        return result;
    }
}
