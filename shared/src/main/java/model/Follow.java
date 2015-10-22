package model;

/**
 * Created by xyllan on 22.10.2015.
 */
public class Follow {
    private long followerId;
    private long followeeId;

    public long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(long followerId) {
        this.followerId = followerId;
    }

    public long getFolloweeId() {
        return followeeId;
    }

    public void setFolloweeId(long followeeId) {
        this.followeeId = followeeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Follow follow = (Follow) o;

        if (followerId != follow.followerId) return false;
        if (followeeId != follow.followeeId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (followerId ^ (followerId >>> 32));
        result = 31 * result + (int) (followeeId ^ (followeeId >>> 32));
        return result;
    }
}
