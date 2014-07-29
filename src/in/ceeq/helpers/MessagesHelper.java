package in.ceeq.helpers;

import in.ceeq.actions.Phone;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.SmsManager;

public class MessagesHelper {

	private SmsManager smsManager;
	private PreferencesHelper preferencesHelper;
	private ContentResolver resolver;
	private Context context;

	/**
	 * 
	 * Message types that may be requested to be send by the helper
	 * 
	 */
	public enum MessageType {
		SIM_CHANGE, LOCATION, PROTECT_ME, NEW_LOCATION, CALLS, NOW, FAIL
	}

	public MessagesHelper(Context context) {
		this.context = context;
		preferencesHelper = PreferencesHelper.getInstance(context);
		smsManager = SmsManager.getDefault();
	}

	/**
	 * Get an instance of the helper
	 * 
	 * @param context
	 * @return
	 */
	public static MessagesHelper getInstance(Context context) {
		return new MessagesHelper(context);
	}

	/**
	 * Send message based on message type
	 * 
	 * @param deliverTo
	 * @param messageType
	 */
	public void sendMessage(String deliverTo, MessageType messageType) {
		String message = "";
		switch (messageType) {
		case CALLS:
			message = getCallsMessage();
			break;
		case LOCATION:
			message = getLastLocationMessage();
			Logger.d(message);
			break;
		case NEW_LOCATION:
			message = getNewLocationMessage();
			Logger.d(message);
			break;
		case NOW:
			message = getDetailsMessage();
			Logger.d(message);
			break;
		case PROTECT_ME:
			message = getProtectMeMessage();
			Logger.d(message);
			break;
		case SIM_CHANGE:
			message = getSimChangeMessage();
			break;
		case FAIL:
			message = getFailedChangeMessage();
			break;
		default:
			break;
		}

		try {
			String senderAddress = preferencesHelper.getString(PreferencesHelper.SENDER_ADDRESS);
			if (!message.isEmpty())
				smsManager
						.sendTextMessage(senderAddress, null, message, null, null);
		} catch (Exception exception) {
			Logger.d("Either the mobile number empty or not correct.");
		}
	}

	private String getFailedChangeMessage() {
		return "Sorry, The PIN entered by you is incorrect.";
	}

	/**
	 * Create a call log message message
	 * 
	 * @return
	 */
	public String getCallsMessage() {
		return "Last 10 calls from device are : " + getCalls(10);
	}

	/**
	 * Create a last location message
	 * 
	 * @return
	 */
	public String getLastLocationMessage() {
		return "Last location of device is : " + getLocationMessage();
	}

	/**
	 * Create a new location message
	 * 
	 * @return
	 */
	public String getNewLocationMessage() {
		return "Device location has changed. New location is : "
				+ getLocationMessage();
	}

	/**
	 * Create raw location message
	 * 
	 * @return
	 */
	public String getLocationMessage() {
		return preferencesHelper
				.getString(PreferencesHelper.LAST_LOCATION_LATITUDE)
				+ ", "
				+ preferencesHelper
						.getString(PreferencesHelper.LAST_LOCATION_LATITUDE);
	}

	public static final String NUMBER = CallLog.Calls.NUMBER;
	public static final String DURATION = CallLog.Calls.DURATION;
	public static final String TYPE = CallLog.Calls.TYPE;
	private final Uri URI = CallLog.Calls.CONTENT_URI;

	/**
	 * Get last n calls from the logs
	 * 
	 * @return String
	 */
	public String getCalls(int n) {
		resolver = context.getContentResolver();
		Cursor cs = resolver.query(URI, null, null, null, null);
		StringBuffer sb = new StringBuffer();
		int count = 0;
		try {
			if (cs.moveToFirst()) {
				do {
					if (cs.getInt(cs.getColumnIndex(TYPE)) != 3) {
						String number = cs.getString(cs.getColumnIndex(NUMBER));
						String duration = cs.getString(cs
								.getColumnIndex(DURATION));
						int durations = (Integer.parseInt(duration) / 60);
						sb.append(number + " " + durations + "mins\n");
						count++;
					}
				} while (cs.moveToNext() && count < n);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cs.close();
		}
		return sb.toString();
	}

	/**
	 * Create SIM changed message
	 * 
	 * @return
	 */
	public String getSimChangeMessage() {
		return preferencesHelper.getString("emergencyMessage") + "\n"
				+ "New Sim Number : " + Phone.get(Phone.SIM_ID, context)
				+ "\n" + "New Sim Operator : "
				+ Phone.get(Phone.OPERATOR, context) + "\n"
				+ "New Sim Subscriber Id : "
				+ Phone.get(Phone.IMSI, context) + "\n"
				+ "Your Device IEMI: " + Phone.get(Phone.IEMI, context)
				+ "\n";
	}

	/**
	 * Create protect me message
	 * 
	 * @return
	 */
	public String getProtectMeMessage() {
		return "Help "
				+ preferencesHelper
						.getString(PreferencesHelper.ACCOUNT_USER_NAME)
				+ preferencesHelper
						.getString(PreferencesHelper.DISTRESS_MESSAGE)
				+ "\n"
				+ "Last User Location : "
				+ getLocationMessage()
				+ "\n"
				+ "Battery Status : "
				+ Phone.get(Phone.BATTERY_LEVEL, context)
				+ "\nCeeq will send you regular location updates every 10 minutes.\n";
	}

	/**
	 * Get current phone details
	 * 
	 * @return
	 */
	public String getDetailsMessage() {
		return "Current \n" + "Sim Number : "
				+ Phone.get(Phone.SIM_ID, context) + "\n"
				+ "Sim Operator : " + Phone.get(Phone.OPERATOR, context)
				+ "\n" + "Sim Subscriber Id : "
				+ Phone.get(Phone.IMSI, context) + "\n" + "Location :"
				+ getLocationMessage() + "\n";
	}
}
