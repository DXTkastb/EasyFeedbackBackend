package com.feedbackBackendApp.dbservice;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.feedbackBackendApp.responsedata.FinalFeedBack;
import com.feedbackBackendApp.responsedata.Sentence;

@Service
public class AsyncDbService {
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public AsyncDbService(@Qualifier("dbsource2") DataSource datasource2) {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource2);
	}

	@Async
	public void batchMessageInsert(FinalFeedBack finalFeedBackData, int primaryKey) {
		if (finalFeedBackData.getIsInaccurate() == 0) {

			List<Sentence> list = finalFeedBackData.getSentences();
			int length = list.size();
			int index = 0;
			MapSqlParameterSource[] resourcesList = new MapSqlParameterSource[length];

			for (Sentence value : list) {

				MapSqlParameterSource mapSqlParameterSource2 = new MapSqlParameterSource();

				mapSqlParameterSource2.addValue("mid", primaryKey);
				mapSqlParameterSource2.addValue("message", value.getText().getContent());
				mapSqlParameterSource2.addValue("messagescore", value.getSentiment().getScore());

				resourcesList[index] = mapSqlParameterSource2;
				index = index + 1;
			}

			namedParameterJdbcTemplate.batchUpdate(
					"INSERT INTO MESSAGES (MESSAGE,MESSAGE_SCORE,FEEDBACK_MID) VALUES( :message, :messagescore, :mid)",
					resourcesList);
		}
	}
}
