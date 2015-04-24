package com.caorenhao.wbcrawler.models;

import java.io.Serializable;

import com.caorenhao.util.NetUtil;
import com.caorenhao.wbcrawler.WBCrawlHelper;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

/**
 * 微博用户
 * 新浪对应 User
 * @author vernkin
 *
 */
@Entity(noClassnameStored = true)
public class WeiboUser implements Serializable {
	
	private static final long serialVersionUID = 2433413309644541685L;

	/** 由sourceType 和 UserId 合并的 */
	@Id
	private String id;
	
	/** 微博来源 */
	@Indexed
	private int sourceType;
	
	/** 用户本身的UID */
	private String userId;
	/** 微博昵称 */
	private String screenName;
	/** 微博名称 */
	private String Name;
	/** 省份编码（参考省份编码表） */
	private int province;
	/** 城市编码（参考城市编码表） */
	private int city;
	/** 地址 */
	private String location;
	/** 个人描述 */
	private String description;
	/** 大头像地址 */
	private String avatarLarge;	
	/** 用户博客地址 */
	private String url;
	/** 自定义图像 */
	private String profileImageUrl;
	/** 用户个性化URL */
	private String userDomain;
	/** 性别,1--男，2--女,0--未知 */
	private int gender = 0;
	/** 粉丝数 */
	private int followersCount;
	/** 关注数 */
	private int friendsCount;
	/** 互粉数 */
	private int biFollowersCount;
	/** 微博数 */
	private int weiboCount;
	/** 创建时间(Unix时间戳) */
	private long createdAt;
	/** 加V标示，是否微博认证用户 */
	private boolean verified;
	/** 认证类型 */
	private int verifiedType;
	/** 认证原因 */
	private String verifiedReason;
	/** 最后更新时间(Unix时间戳) */
	private long updateAt;
	
	public WeiboUser() {		
	}
	
	/** 
	 * 生成ID的构造函数 , 生成最后的更新时间
	 * @param sourceType 微博来源
	 * @param userId 本身的userId
	 */
	public WeiboUser(int sourceType, String userId) {		
		this.id = WBCrawlHelper.generateId(sourceType, userId);
		this.sourceType = sourceType;
		this.userId = userId;
		updateAt = NetUtil.getTimeInSecs();
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	public int getSourceType() {
		return sourceType;
	}

	public String getVerified_reason() {
		return verifiedReason;
	}
	public void setVerified_reason(String verifiedReason) {
		this.verifiedReason = verifiedReason;
	}
	public void setUserId(String id) {
		this.userId = id;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	public void setName(String Name) {
		this.Name = Name;
	}

	public void setProvince(int province) {
		this.province = province;
	}
	public void setCity(int city) {
		this.city = city;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}
	public void setUserDomain(String userDomain) {
		this.userDomain = userDomain;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public void setFollowersCount(int followersCount) {
		this.followersCount = followersCount;
	}
	public void setFriendsCount(int friendsCount) {
		this.friendsCount = friendsCount;
	}
	
	public void setWeiboCount(int weiboCount) {
		this.weiboCount = weiboCount;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}
	
	public void setVerifiedType(int verifiedType) {
		this.verifiedType = verifiedType;
	}
	
	public void setAvatarLarge(String avatarLarge) {
		this.avatarLarge = avatarLarge;
	}

	public void setBiFollowersCount(int biFollowersCount) {
		this.biFollowersCount = biFollowersCount;
	}

	public String getVerifiedReason() {
		return verifiedReason;
	}
	public void setVerifiedReason(String verifiedReason) {
		this.verifiedReason = verifiedReason;
	}

	public String getUrl() {
		return url;
	}
	
	public String getProfileImageURL() {
		return profileImageUrl;
	}
	
	public int getVerifiedType() {
		return verifiedType;
	}

	public String getAvatarLarge() {
		return avatarLarge;
	}

	
	public int getBiFollowersCount() {
		return biFollowersCount;
	}
	
	
	public String getUserId() {
		return userId;
	}

	public String getScreenName() {
		return screenName;
	}
	
	public String getName() {
		return Name;
	}
	
	public int getProvince() {
		return province;
	}

	public int getCity() {
		return city;
	}

	public String getLocation() {
		return location;
	}

	public String getDescription() {
		return description;
	}

	public String getUserDomain() {
		return userDomain;
	}

	public int getGender() {
		return gender;
	}

	public int getFollowersCount() {
		return followersCount;
	}

	public int getFriendsCount() {
		return friendsCount;
	}

	public int getWeiboCount() {
		return weiboCount;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public boolean isVerified() {
		return verified;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeiboUser other = (WeiboUser) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "User [" +
		"id=" + userId +
		", screenName=" + screenName + 
		", province=" + province + 
		", city=" + city +
		", location=" + location + 
		", description=" + description + 
		", url=" + url + 
		", profileImageUrl=" + profileImageUrl + 
		", userDomain=" + userDomain + 
		", gender=" + gender + 
		", followersCount=" + followersCount + 
		", friendsCount=" + friendsCount + 
		", weiboCount=" + weiboCount + 
		", verified=" + verified + 
		", verifiedType=" + verifiedType + 
		", avatarLarge=" + avatarLarge + 
		", biFollowersCount=" + biFollowersCount + 
		", verifiedReason="  + verifiedReason +
		"]";
	}

	public void setUpdateAt(long updateAt) {
		this.updateAt = updateAt;
	}

	public long getUpdateAt() {
		return updateAt;
	}
}
