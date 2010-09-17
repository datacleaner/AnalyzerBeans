package org.eobjects.analyzer.result.renderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import org.eobjects.analyzer.annotations.RendererBean;
import org.eobjects.analyzer.result.DateGapAnalyzerResult;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.analyzer.util.TimeInterval;

@RendererBean(TextRenderingFormat.class)
public class DateGapTextRenderer implements
		Renderer<DateGapAnalyzerResult, String> {

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public String render(DateGapAnalyzerResult result) {
		Set<String> names = result.getGroupNames();
		if (names.isEmpty()) {
			return "No timelines recorded";
		}

		StringBuilder sb = new StringBuilder();
		for (String name : names) {
			if (!StringUtils.isNullOrEmpty(name)) {
				sb.append("Timeline recorded for '");
				sb.append(name);
				sb.append('\'');
				sb.append('\n');
			}

			SortedSet<TimeInterval> gaps = result.getGaps().get(name);
			if (gaps.isEmpty()) {
				sb.append(" - no time gaps!\n");
			} else {
				for (TimeInterval timeInterval : gaps) {
					sb.append(" - time gap: ");
					sb.append(format(timeInterval));
					sb.append('\n');
				}
			}

			// TODO: Include overlaps
			SortedSet<TimeInterval> overlaps = result.getOverlaps().get(name);
			if (overlaps.isEmpty()) {
				if (overlaps.isEmpty()) {
					sb.append(" - no time overlaps!\n");
				} else {
					for (TimeInterval timeInterval : overlaps) {
						sb.append(" - time overlap: ");
						sb.append(format(timeInterval));
						sb.append('\n');
					}
				}
			}
		}
		return sb.toString();
	}

	private String format(TimeInterval interval) {
		Date from = new Date(interval.getFrom());
		Date to = new Date(interval.getTo());
		return df.format(from) + " to " + df.format(to);
	}
}
