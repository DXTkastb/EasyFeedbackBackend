package com.feedbackBackendApp.dbservice;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.feedbackBackendApp.responsedata.Sentence;
import com.feedbackBackendApp.responsedata.Sentiment;
import com.feedbackBackendApp.responsedata.TextSpan;

public class SentenceMapper implements RowMapper<Sentence>{

	@Override
	public Sentence mapRow(ResultSet rs, int rowNum) throws SQLException {
		Sentence sentence =  new Sentence();
		TextSpan textSpan = new TextSpan();
		textSpan.setContent(rs.getString(1));
		Sentiment messageSentiment = new Sentiment();
		messageSentiment.setScore(rs.getDouble(2));
		sentence.setText(textSpan);
		sentence.setSentiment(messageSentiment);
		return sentence;
	}

}
