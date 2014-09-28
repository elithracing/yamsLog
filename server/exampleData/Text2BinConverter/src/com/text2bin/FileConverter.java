package com.text2bin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import protobuf.Protocol.AttributeConfiguration;
import protobuf.Protocol.DataMsg;
import protobuf.Protocol.ExperimentMetadataMsg.MetadataStruct;
import protobuf.Protocol.GeneralMsg;
import protobuf.Protocol.GeneralMsg.SubType;
import protobuf.Protocol.SensorConfiguration;

public class FileConverter {

	private static final boolean DEBUG = false;

	/**
	 * Takes the file path as argument
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"Usage: java FileConverter <input file> <experiment name>");
		}

		FileConverter fc = new FileConverter();
		fc.convert(args[0], args[1]);
	}

	public void convert(String inputFile, String experimentName)
			throws IOException {
		File experimentFolderPath = new File(experimentName);
		if (!experimentFolderPath.mkdir()) {
			throw new IOException("Experiment directory already exists");
		}
		String dataFileName = experimentName + ".data";
		String metadataFileName = experimentName + ".meta";
		String textFileName = experimentName + ".txt";

		Set<Integer> allSensorIds = new HashSet<>();

		try (FileOutputStream dataOutputStream = new FileOutputStream(new File(
				experimentFolderPath, dataFileName))) {
			try (BufferedReader br = new BufferedReader(new FileReader(
					inputFile))) {
				System.out.println("Converting input file to " + dataFileName
						+ "...");

				String line = br.readLine();

				long numLines = 0;
				long startTime = System.currentTimeMillis();

				while (line != null) {
					// remove empty lines and comments
					if (!(line.isEmpty() || line.startsWith("%%"))) {
						GeneralMsg msg = parse(line.split(","));
						if (msg.getSubType() == SubType.DATA_T) {
							allSensorIds.add(msg.getData().getTypeId());
						}
						msg.writeDelimitedTo(dataOutputStream);

						if (DEBUG) {
							System.out.println(msg.toString());
						}
					}
					line = br.readLine();
					numLines++;
				}
				float time = (System.currentTimeMillis() - startTime) / 1000;
				System.out.println("Conversion successful ("
						+ (int) (numLines / time) + " lines per second)");

				System.out.println("Writing metadata to " + metadataFileName
						+ "...");
				writeMetadata(experimentFolderPath, metadataFileName,
						allSensorIds);
				System.out.println("Metadata write successful");

				System.out.println("Copying input file to " + textFileName
						+ "...");
				Files.copy(new File(inputFile).toPath(), new FileOutputStream(
						new File(experimentFolderPath, textFileName)));
				System.out.println("Copying successful");
			}
		}
	}

	private void writeMetadata(File experimentFolderPath,
			String metadataFileName, Set<Integer> allSensorIds)
			throws FileNotFoundException, IOException {
		MetadataStruct.Builder metadataStructBuilder = MetadataStruct
				.newBuilder();
		for (Integer id : allSensorIds) {
			SensorConfiguration.Builder sensorConfigurationBuilder = SensorConfiguration
					.newBuilder();
			sensorConfigurationBuilder.setSensorId(id);
			sensorConfigurationBuilder.setMaxAttributes(20); // TODO
			// TODO: Add attributes?
			switch (id) {
			case 0: {
				sensorConfigurationBuilder.setName("imucam");
				addAttributeConfigurations(sensorConfigurationBuilder,
						new String[] { "accX", "accY", "accZ", "gyrX", "gyrY",
								"gyrZ", "magX", "magY", "magZ", "roll",
								"pitch", "yaw" });
				break;
			}
			case 1: {
				sensorConfigurationBuilder.setName("imucar");
				addAttributeConfigurations(sensorConfigurationBuilder,
						new String[] { "accX", "accY", "accZ", "gyrX", "gyrY",
								"gyrZ", "magX", "magY", "magZ", "roll",
								"pitch", "yaw" });
				break;
			}
			case 2: {
				sensorConfigurationBuilder.setName("carcan1");
				addAttributeConfigurations(sensorConfigurationBuilder,
						new String[] { "CAN_ID", "CAN_message_length" });
				break;
			}
			case 3: {
				sensorConfigurationBuilder.setName("can");
				addAttributeConfigurations(sensorConfigurationBuilder,
						new String[] { "CAN_ID", "CAN_message_length" });
				break;
			}
			case 4: {
				sensorConfigurationBuilder.setName("gps");
				addAttributeConfigurations(sensorConfigurationBuilder,
						new String[] { "UT-Time", "Lat", "Lon", "Speed(m/s)",
								"Course/Heading" });
				break;
			}
			case 30: {
				sensorConfigurationBuilder.setName("corrsys");
				addAttributeConfigurations(sensorConfigurationBuilder,
						new String[] { "abstime", "starttime", "h1", "h2",
								"h3", "hc1", "hc2", "hc3", "pitch", "roll",
								"roll2", "pitchrate", "rollrate", "roll2rate",
								"vabs", "vlat", "vtrans", "slipangle" });
				break;
			}
			}
			metadataStructBuilder
					.addSensorConfigurations(sensorConfigurationBuilder.build());
		}
		MetadataStruct metadataStruct = metadataStructBuilder.build();

		try (FileOutputStream fos = new FileOutputStream(new File(
				experimentFolderPath, metadataFileName))) {
			metadataStruct.writeDelimitedTo(fos);
		}
	}

	private void addAttributeConfigurations(
			SensorConfiguration.Builder sensorConfigurationBuilder,
			String[] attrs) {
		for (int i = 0; i < attrs.length; i++) {
			AttributeConfiguration.Builder acBuilder = AttributeConfiguration
					.newBuilder();
			acBuilder.setIndex(i);
			acBuilder.setName(attrs[i]);
			sensorConfigurationBuilder.addAttributeConfigurations(acBuilder
					.build());
		}
	}

	private GeneralMsg parse(String[] fields) {
		double time = Double.parseDouble(fields[0]);
		int id = Integer.parseInt(fields[1]);

		GeneralMsg.Builder generalMsgBuilder = GeneralMsg.newBuilder();
		generalMsgBuilder.setSubType(SubType.DATA_T);
		DataMsg.Builder builder = DataMsg.newBuilder();

		builder.setTime(time);
		builder.setTypeId(id);

		for (int i = 2; i < fields.length; i++) {
			builder.addData(Float.parseFloat(fields[i]));
		}
		generalMsgBuilder.setData(builder.build());

		return generalMsgBuilder.build();
	}
}
