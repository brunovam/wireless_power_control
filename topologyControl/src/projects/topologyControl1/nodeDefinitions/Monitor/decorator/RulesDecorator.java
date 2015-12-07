package projects.topologyControl1.nodeDefinitions.Monitor.decorator;

import java.util.List;

import projects.topologyControl1.nodeDefinitions.Monitor.DataMessage;
import projects.topologyControl1.nodeDefinitions.Monitor.IMonitor;
import projects.topologyControl1.nodeDefinitions.Monitor.Rules;
import sinalgo.nodes.Node;

public class RulesDecorator implements IMonitor {
	
	private IMonitor monitor;

	public void doInference() {
		monitor.doInference();

	}
	
	public RulesDecorator(IMonitor monitor) {
		this.monitor = monitor;		
	}

	public List<DataMessage> getDataMessage() {
		return monitor.getDataMessage();
	}

	public void setLocalMaliciousList(Rules rule, List<Node> lista) {
		monitor.setLocalMaliciousList(rule, lista);		
	}

	public Integer getMonitorID() {
		return monitor.getMonitorID();
	}
	
	

}
