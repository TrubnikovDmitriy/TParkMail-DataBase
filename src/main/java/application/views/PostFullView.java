package application.views;

import application.models.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class PostFullView {


	@JsonProperty(value = "post")
	private PostModel postModel;
	@JsonProperty(value = "author")
	private UserModel userModel;
	@JsonProperty(value = "forum")
	private ForumModel forumModel;
	@JsonProperty(value = "thread")
	private ThreadModel threadModel;

	public PostFullView(PostModel postModel,
	                    UserModel userModel,
	                    ForumModel forumModel,
	                    ThreadModel threadModel) {
		this.postModel = postModel;
		this.userModel = userModel;
		this.forumModel = forumModel;
		this.threadModel = threadModel;
	}



	public UserModel getUserModel() {
		return userModel;
	}

	public void setUserModel(UserModel userModel) {
		this.userModel = userModel;
	}

	public ForumModel getForumModel() {
		return forumModel;
	}

	public void setForumModel(ForumModel forumModel) {
		this.forumModel = forumModel;
	}

	public PostModel getPostModel() {
		return postModel;
	}

	public void setPostModel(PostModel postModel) {
		this.postModel = postModel;
	}

	public ThreadModel getThreadModel() {
		return threadModel;
	}

	public void setThreadModel(ThreadModel threadModel) {
		this.threadModel = threadModel;
	}
}
