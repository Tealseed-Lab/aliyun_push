package com.aliyun.ams.push;

import com.alibaba.sdk.android.push.HonorRegister;
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister;
// import com.alibaba.sdk.android.push.register.GcmRegister;
import com.alibaba.sdk.android.push.register.MeizuRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;
import com.alibaba.sdk.android.push.register.OppoRegister;
import com.alibaba.sdk.android.push.register.VivoRegister;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Locale;

/**
 * @author wangyun
 * @date 2023/1/13
 */
public class AliyunThirdPushUtils {

	private static String getAppMetaDataWithId(Context context, String key) {
		String value = getAppMetaData(context, key);
		if (value != null && value.startsWith("id=")) {
			return value.replace("id=", "");
		}
		return value;
	}

	public static String getAppMetaData(Context context, String key) {
		try {
			final PackageManager packageManager = context.getPackageManager();
			final String packageName = context.getPackageName();
			ApplicationInfo info = packageManager.getApplicationInfo(packageName,
				PackageManager.GET_META_DATA);
			if (info != null && info.metaData != null && info.metaData.containsKey(key)) {
				return String.valueOf(info.metaData.get(key));
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 当前设备是不是这些品牌之一。
	 *
	 * 厂商辅助通道只在自家 ROM 上才建立得起长连接：在 OPPO 手机上注册小米推送，
	 * 除了拿不到 token 之外没有任何作用。但注册这一下并非无害——小米 SDK 在注册
	 * 过程中会连续读取设备序列号（Build.getSerial → ro.serialno），隐私合规检测
	 * 会判定为「第三方 SDK 超频次获取相关信息」。
	 *
	 * 实测（OPPO 安全隐私自动化检测，敲敲 2.12.0，2026-09-06）：一次注册里 80ms
	 * 内读了 6 次序列号，检测项 2.5 因此不通过。
	 *
	 * 判断用 BRAND 与 MANUFACTURER 拼起来匹配：子品牌（Redmi、iQOO、realme 等）
	 * 在这两个字段里的取值并不一致，只看其一会漏。
	 */
	private static boolean isBrand(String... keywords) {
		String identity = ((Build.BRAND == null ? "" : Build.BRAND) + " "
			+ (Build.MANUFACTURER == null ? "" : Build.MANUFACTURER)).toLowerCase(Locale.ROOT);
		for (String keyword : keywords) {
			if (identity.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	public static void registerGCM(Application application) {
		String sendId = getGCMSendId(application);
		String applicationId = getGCMApplicationId(application);
		String projectId = getGCMProjectId(application);
		String apiKey = getGCMApiKey(application);

		// if (sendId != null && applicationId != null && projectId != null && apiKey != null) {
		// 	GcmRegister.register(application, sendId, applicationId, projectId, apiKey);
		// }
	}

	private static String getGCMSendId(Context context) {
		return getAppMetaDataWithId(context, "com.gcm.push.sendid");
	}

	private static String getGCMApplicationId(Context context) {
		return getAppMetaDataWithId(context, "com.gcm.push.applicationid");
	}

	private static String getGCMProjectId(Context context) {
		return getAppMetaDataWithId(context, "com.gcm.push.projectid");
	}

	private static String getGCMApiKey(Context context) {
		return getAppMetaDataWithId(context, "com.gcm.push.api.key");
	}

	public static void registerMeizuPush(Application application) {
		// 非魅族设备直接返回，见 isBrand 的说明。
		if (!isBrand("meizu")) {
			return;
		}
		String meizuId = getMeizuPushId(application);
		String meizuKey = getMeizuPushKey(application);

		if (meizuId != null && meizuKey != null) {
			MeizuRegister.register(application, meizuId, meizuKey);
		}
	}

	private static String getMeizuPushId(Context context) {
		return getAppMetaDataWithId(context, "com.meizu.push.id");
	}

	private static String getMeizuPushKey(Context context) {
		return getAppMetaDataWithId(context, "com.meizu.push.key");
	}

	public static void registerOppoPush(Application application) {
		// 非OPPO 系设备直接返回，见 isBrand 的说明。
		if (!isBrand("oppo", "oneplus", "realme")) {
			return;
		}
		String oppoKey = getOppoPushKey(application);
		String oppoSecret = getOppoPushSecret(application);

		if (oppoKey != null && oppoSecret != null) {
			OppoRegister.register(application, oppoKey, oppoSecret);
		}
	}

	private static String getOppoPushKey(Context context) {
		return getAppMetaDataWithId(context, "com.oppo.push.key");
	}

	private static String getOppoPushSecret(Context context) {
		return getAppMetaDataWithId(context, "com.oppo.push.secret");
	}

	public static void registerXiaoMiPush(Application application) {
		// 非小米系设备直接返回，见 isBrand 的说明。
		if (!isBrand("xiaomi", "redmi", "poco")) {
			return;
		}
		String xiaoMiId = getXiaoMiId(application);
		String xiaoMiKey = getXiaoMiKey(application);

		if (xiaoMiId != null && xiaoMiKey != null) {
			MiPushRegister.register(application, xiaoMiId, xiaoMiKey);
		}
	}

	private static String getXiaoMiId(Context context) {
		return getAppMetaDataWithId(context, "com.xiaomi.push.id");
	}

	private static String getXiaoMiKey(Context context) {
		return getAppMetaDataWithId(context, "com.xiaomi.push.key");
	}

	public static  void registerVivoPush(Application application) {
		// 非vivo 系设备直接返回，见 isBrand 的说明。
		if (!isBrand("vivo", "iqoo")) {
			return;
		}
		String vivoApiKey = getVivoApiKey(application);
		String vivoAppId = getVivoAppId(application);
		if (vivoApiKey != null && vivoAppId != null) {
			VivoRegister.register(application);
		}
	}

	private static String getVivoApiKey(Context context) {
		return getAppMetaData(context, "com.vivo.push.api_key");
	}

	private static String getVivoAppId(Context context) {
		return getAppMetaData(context, "com.vivo.push.app_id");
	}


	public static void registerHuaweiPush(Application application) {
		// 非华为设备直接返回，见 isBrand 的说明。
		if (!isBrand("huawei")) {
			return;
		}
		String huaweiAppId = getHuaWeiAppId(application);
		if (huaweiAppId != null) {
			HuaWeiRegister.register(application);
		}
	}

	private static String getHuaWeiAppId(Context context) {
		String value = getAppMetaData(context, "com.huawei.hms.client.appid");
		if (value != null && value.startsWith("appid=")) {
			return value.replace("appid=", "");
		}
		return null;
	}

	public static void registerHonorPush(Application application) {
		// 非荣耀设备直接返回，见 isBrand 的说明。
		if (!isBrand("honor")) {
			return;
		}
		String honorAppId = getHonorAppId(application);
		if (honorAppId != null) {
			HonorRegister.register(application);
		}
	}

	private static String getHonorAppId(Context context) {
		return getAppMetaData(context, "com.hihonor.push.app_id");
	}
}
