package com.demo.flink.streaming;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class StreamingJob {

	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

		// Kafka config
		Properties props = new Properties();
		props.setProperty("bootstrap.servers", "my-cluster-kafka-bootstrap.openshift-operators.svc:9092");
		props.setProperty("group.id", "test");

		// Kafka source reading raw JSON strings
		FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
				"call-records", new SimpleStringSchema(), props);
		DataStream<String> rawStream = env.addSource(consumer);

		ObjectMapper mapper = new ObjectMapper();

		// Parse JSON into TowerEvent POJO
		DataStream<TowerEvent> events = rawStream.map(line -> {
			JsonNode node = mapper.readTree(line);
			String cellId = node.get("cell_id").asText();
			double lat = node.get("lat").asDouble();
			double lng = node.get("lng").asDouble();
			boolean isDropped = node.get("is_dropped").asBoolean();
			return new TowerEvent(cellId, lat, lng, isDropped);
		}).returns(TowerEvent.class);

		// Send raw records to Supabase
		AsyncDataStream.unorderedWait(rawStream, new SupabaseSink(), 30, TimeUnit.SECONDS, 100);

		// Aggregate by cell_id
		KeyedStream<TowerEvent, String> keyed = events.keyBy((KeySelector<TowerEvent, String>) TowerEvent::getCellId);

		// Drop rate alert stream
		SingleOutputStreamOperator<String> alerts = keyed
				.countWindow(30, 1)
				.aggregate(new DropRateAggregator(), new AttachCellId());

		// Kafka sink for alerts
		FlinkKafkaProducer<String> alertProducer = new FlinkKafkaProducer<>(
				"dropped-alerts", new SimpleStringSchema(), props);
		alerts.addSink(alertProducer);

		env.execute("Flink Streaming Drop Rate Alert by Cell ID");
	}

	// POJO for incoming call records
	public static class TowerEvent {
		public String cellId;
		public double lat;
		public double lng;
		public boolean isDropped;

		public TowerEvent() {}

		public TowerEvent(String cellId, double lat, double lng, boolean isDropped) {
			this.cellId = cellId;
			this.lat = lat;
			this.lng = lng;
			this.isDropped = isDropped;
		}

		public String getCellId() {
			return cellId;
		}
	}

	// Aggregator: computes drop rate from events
	public static class DropRateAggregator implements AggregateFunction<TowerEvent, int[], String> {
		@Override
		public int[] createAccumulator() {
			return new int[]{0, 0}; // [total, dropped]
		}

		@Override
		public int[] add(TowerEvent value, int[] acc) {
			acc[0]++;
			if (value.isDropped) acc[1]++;
			return acc;
		}

		@Override
		public String getResult(int[] acc) {
			double dropRate = (double) acc[1] / acc[0];
			return String.format("\"dropRate\": %.2f, \"windowSize\": %d",
					dropRate, acc[0]);
		}

		@Override
		public int[] merge(int[] a, int[] b) {
			return new int[]{a[0] + b[0], a[1] + b[1]};
		}
	}

	// Appends cell_id to output and filters alerts by drop rate
	public static class AttachCellId extends ProcessWindowFunction<String, String, String, GlobalWindow> {
		@Override
		public void process(String cellId, Context context, Iterable<String> values, Collector<String> out) {
			String metrics = values.iterator().next();
			String result = String.format("{\"cell_id\": \"%s\", %s}", cellId, metrics);
			out.collect(result);
		}
	}
}
