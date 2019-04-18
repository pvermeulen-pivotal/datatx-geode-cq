package datatx.util.geode.cq;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.geode.cache.query.internal.cq.CqServiceImpl;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.query.CqClosedException;
import org.apache.geode.cache.query.CqQuery;

public class CloseServerCqs implements Function, Declarable {
	private static final long serialVersionUID = -4470966767486248329L;
	private final Logger LOG = LogManager.getLogger(CloseServerCqs.class);

	public List<String> closeCqs() {
		List<String> closedCqs = new ArrayList<String>();
		InternalCache ic = (InternalCache) CacheFactory.getAnyInstance();
		CqQuery[] cqs = CacheFactory.getAnyInstance().getQueryService().getCqs();
		for (int i = 0; i < cqs.length; i++) {
			if (cqs[i].isRunning()) {
				closedCqs.add(cqs[i].getName());
			}
		}
		if (closedCqs.size() > 0) {
			CqServiceImpl impl = (CqServiceImpl) ic.getCqService();
			try {
				impl.closeAllCqs(true);
			} catch (CqClosedException e) {
				LOG.error("Exception closing CQs - Error: " + e.getMessage(), e);
			}
		}
		return closedCqs;
	}

	@Override
	public void execute(FunctionContext fc) {
		List<String> closedCqs = closeCqs();
		if (closedCqs.size() == 0) {
			closedCqs.add("No CQ queries active");
		}
		fc.getResultSender().lastResult(closedCqs);
	}

	@Override
	public String getId() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean hasResult() {
		return true;
	}

	@Override
	public boolean isHA() {
		return true;
	}

	@Override
	public boolean optimizeForWrite() {
		return false;
	}
}
