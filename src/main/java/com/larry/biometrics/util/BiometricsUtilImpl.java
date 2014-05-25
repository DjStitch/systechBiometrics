/**
 * 
 */
package com.larry.biometrics.util;

import org.apache.log4j.Logger;

import SecuGen.FDxSDKPro.jni.JSGFPLib;
import SecuGen.FDxSDKPro.jni.SGDeviceInfoParam;
import SecuGen.FDxSDKPro.jni.SGFDxErrorCode;
import SecuGen.FDxSDKPro.jni.SGFingerInfo;

import com.larry.biometrics.model.Pensioner;
import com.larry.biometrics.query.PensionerBioQueryAdapter;

/**
 * @author Otieno Lawrence
 * 
 */
public class BiometricsUtilImpl implements BiometricsUtil {

	private static Logger logger = Logger.getLogger(BiometricsUtil.class);
	public Pensioner currentPensioner;
	private PensionerBioQueryAdapter adapter;
	private FundMasterConfiguration configuration;

	private JSGFPLib fplib = null;

	public BiometricsUtilImpl() {
		fplib = new JSGFPLib();
		configuration = new FundMasterConfiguration();
		adapter = new PensionerBioQueryAdapter(configuration.getUrl(),
				configuration.getUserName(), configuration.getPassword());
	}

	public long initDevice(long deviceName) {
		assert (fplib != null);
		return fplib.Init(deviceName);
	}

	public long openDevice(long deviceName) {
		return fplib.OpenDevice(deviceName);
	}

	public long getDeviceInfo(SGDeviceInfoParam deviceInfo) {
		return fplib.GetDeviceInfo(deviceInfo);
	}

	public long verify(byte[] verifyMin, long securityLevel) {
		Pensioner pensioner = adapter.getPensionerInfo(currentPensioner
				.getPensionerNumber());
		byte[] registeredMin = pensioner.getFpMinutiae();
		boolean[] matched = new boolean[1];
		matched[0] = false;
		return fplib.MatchTemplate(registeredMin, verifyMin, securityLevel,
				matched);
	}

	public long register(byte[] registeredMin1, byte[] registeredMin2,
			long securityLevel) {
		currentPensioner.setFpMinutiae(registeredMin2);
		boolean[] matched = new boolean[1];
		long err = fplib.MatchTemplate(registeredMin1, registeredMin2,
				securityLevel, matched);
		if (err == SGFDxErrorCode.SGFDX_ERROR_NONE) {
			currentPensioner.setFpMinutiae(registeredMin2);
			try {
				adapter.savePensionerInfo(currentPensioner);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return err;

	}

	public int getMatchingScore(byte[] regMin1, byte[] regMin2) {
		int matchingScore = 0;
		int[] matchScore = new int[1];
		long iError = fplib.GetMatchingScore(regMin1, regMin2, matchScore);
		if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE) {
			matchingScore = matchScore[0];
		}
		return matchingScore;
	}

	public long configure() {
		return fplib.Configure(0);
	}

	public long setLedOn(boolean bLedOn) {

		return fplib.SetLedOn(bLedOn);
	}

	public long getImageEx(byte[] buffer, long imgWidth, long imgHeight,
			long quality) {
		return fplib.GetImageEx(buffer, imgWidth, imgHeight, quality);
	}

	public int getImageQuality(long imageWidth, long imageHeight,
			byte[] imageBuffer1) {
		int imageQuality = 0;
		int[] quality = new int[1];
		long iError = fplib.GetImageQuality(imageWidth, imageHeight,
				imageBuffer1, quality);
		if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE) {
			imageQuality = quality[0];
		}
		return imageQuality;
	}

	public long createTemplate(SGFingerInfo fingerInfo, byte[] imageBuffer,
			byte[] regMin) {
		return fplib.CreateTemplate(fingerInfo, imageBuffer, regMin);
	}

	public long close() {
		return fplib.Close();
	}

}