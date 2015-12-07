package projects.topologyControl1.nodes.nodeImplementations;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import projects.topologyControl1.enumerators.ChordMessageType;
import projects.topologyControl1.nodeDefinitions.BasicNode;
import projects.topologyControl1.nodeDefinitions.Monitor.DataMessage;
import projects.topologyControl1.nodeDefinitions.Monitor.IMonitor;
import projects.topologyControl1.nodeDefinitions.Monitor.Rules;
import projects.topologyControl1.nodeDefinitions.Monitor.decorator.IntervalRule;
import projects.topologyControl1.nodeDefinitions.Monitor.decorator.RepetitionRule;
import projects.topologyControl1.nodeDefinitions.Monitor.decorator.RetransmissionRule;
import projects.topologyControl1.nodeDefinitions.chord.UtilsChord;
import projects.topologyControl1.nodeDefinitions.dht.Chord;
import projects.topologyControl1.nodeDefinitions.dht.IDHT;
import projects.topologyControl1.nodeDefinitions.dht.Signature;
import projects.topologyControl1.nodes.messages.ChordMessage;
import projects.topologyControl1.nodes.messages.FloodFindDsdv;
import projects.topologyControl1.nodes.messages.FloodFindFuzzy;
import projects.topologyControl1.nodes.messages.PayloadMsg;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.storage.ReusableListIterator;

/**
 * Node responsible for monitoring your neighbors through the promiscuous
 * listening mode. Stores informations of interest, processing them due to
 * specified rules. It can act like a <b>Supervisor Node</b>. In this case, it
 * becomes able to correlate evidences discovered by others monitor nodes. Each
 * supervisor node is responsible for correlating a specific sub-set of rules.
 * 
 * @author Marcus Vin�cius Lemos<br/>
 * @changes Alex Lacerda Ramos
 */
public class MonitorNode extends BasicNode implements IMonitor {

	/**
	 * It stores the internal messages buffer size (<code>dataMessages</code>).
	 */
	public static Integer INTERNAL_BUFFER;

	/**
	 * Messages intercepted by the monitor node. When the messages buffer is
	 * full, the rules must be applied to the messages in order to find
	 * malicious nodes.
	 */
	private List<DataMessage> dataMessages;

	/**
	 * Map that contains a list of possible malicious nodes according to each
	 * rule. This is a local map, hence it contains only the nodes watched by
	 * this monitor node.
	 */
	private Map<Rules, List<Node>> mapLocalMaliciousNodes;

	private Integer hashLength = 1024;
	
	private Integer hashChain[] = new Integer[hashLength];
	
	private IDHT dht;
	
	static {
		try {
			INTERNAL_BUFFER = Configuration
					.getIntegerParameter("Monitor/Inference/InternalBuffer");
		} catch (CorruptConfigurationEntryException e) {
			Tools
					.appendToOutput("Key Monitor/Inference/InternalBuffer not found");
			e.printStackTrace();
		}
	}

	@Override
	public void init() {
		setMyColor(Color.RED);
		super.init();
		this.dht = new Chord(this);
		mapLocalMaliciousNodes = new HashMap<Rules, List<Node>>();
		dataMessages = new ArrayList<DataMessage>();
		initHashChain();
	}

	@Override
	protected void preProcessingMessage(Message message) {
		if (message instanceof PayloadMsg) {
			PayloadMsg msg = (PayloadMsg) message;
			if (!msg.isChordMessage()) {
				addMessageToList(msg);
			}
		}
	}

	@Override
	public Boolean beforeSendingMessage(Message message) {
		if (message instanceof PayloadMsg) {
			PayloadMsg msg = (PayloadMsg) message;
			if (!msg.isChordMessage()) {
				addMessageToList(msg);
			}
		}
		return Boolean.TRUE;
	}
	
	private void addMessageToList(PayloadMsg msg) {
		DataMessage dataMessage = new DataMessage();
		dataMessage.setClock((int) Tools.getGlobalTime());
		dataMessage.setData(msg.value);
		dataMessage.setFinalDst(msg.baseStation.ID);
		dataMessage.setIdMsg(msg.sequenceNumber);
		dataMessage.setImediateDst(msg.nextHop.ID);
		dataMessage.setImediateSrc(msg.imediateSender.ID);
		dataMessage.setSource(msg.sender.ID);

		dataMessages.add(dataMessage);

		if (dataMessages.size() == INTERNAL_BUFFER) {
			applyRules();
			dataMessages.clear();
		}
	}

	private void applyRules() {
		IMonitor rule1 = new RepetitionRule(this);
		IMonitor rule2 = new RetransmissionRule(rule1);
		IMonitor rule3 = new IntervalRule(rule2);
		rule3.doInference();

		
		if(BaseStation.isFingerTableCreated){
			this.sendMaliciousNodesToSupervisor();
		}
	}

	private void sendMaliciousNodesToSupervisor() {
		
		for (Rules rule : mapLocalMaliciousNodes.keySet()) {

			Integer hashKey = UtilsChord.generateSHA1(rule.name());
			MonitorNode sucessorNode = this.getDht().findSucessor(hashKey);

			System.out.println("node" + this.ID + "(hash: " + this.getHashID() + ")" + " is sending malicious for rule: " + rule.name() + 
					" hash(" +hashKey + ") for supervisor " + sucessorNode.ID + " hash(" + sucessorNode.getHashID() + ")" );

			List<Node> maliciousNodes = mapLocalMaliciousNodes.get(rule);
			
			PayloadMsg payloadMsg = new PayloadMsg();
			
			Signature signature = new Signature(this, maliciousNodes, rule, sucessorNode);
			
			payloadMsg.value = ChordMessageType.SEND_TO_SUPERVISOR.getValue();
			payloadMsg.setSignature(signature);
			
			this.getRouting().sendChordMessage(payloadMsg);
		}
		//all the malicious nodes were sent to supervisor, they don't need to be kept here anymore
		this.mapLocalMaliciousNodes.clear();
	}
	
	public void doInference() {
		
	}
	
	public void correlate(Set<Signature> signatures) {
		// TODO Pegar m�todo de correlacionar no projeto antigo do marvin
		System.out.println("##########correlating...###########");
	}
	
	public void addLocalMaliciousList(Rules rule, List<Node> lista) {
		mapLocalMaliciousNodes.put(rule, lista);
	}
	
	@Override
	protected void postProcessingMessage(Message message) {
		super.postProcessingMessage(message);
		
		//depois que os monitores receberem uma mensagem roteamento, eles estao certos de que podem enviar uma
		//mensagem para a baseStation notificando-a de sua exist�ncia
		if(message instanceof FloodFindDsdv || message instanceof FloodFindFuzzy){
			this.sendMessageToBaseStation(ChordMessageType.ANSWER_MONITOR_ID.getValue());
		}
		
		if (message instanceof PayloadMsg) {
			PayloadMsg payloadMsg = (PayloadMsg) message;
			
			//recebendo assinaturas do outros monitores
			if (payloadMsg.value == ChordMessageType.SEND_TO_SUPERVISOR.getValue().intValue()) {
				
				Signature signature = payloadMsg.getSignature();

				if (signature.getSupervisor().equals(this)) {//this node is a supervisor (target) responsible for the signature rule
					this.notifyNeighbors();// notify neighbors that this node is a supervisor
					this.getDht().addExternalSignature(signature);
				}
			}
		}
	}

	private void notifyNeighbors() {
		ChordMessage chordMessage = new ChordMessage();
		chordMessage.setSender(this);
		chordMessage.setChordMessageType(ChordMessageType.NOTIFY_NEIGHBORS);
		
		Edge edge;
		Node neighbour;
		Connections conn = this.outgoingConnections;
		ReusableListIterator<Edge> listConn = conn.iterator();
		
		while (listConn.hasNext()){
			edge = listConn.next();
			neighbour = edge.endNode;
			neighbour.send(chordMessage, neighbour);
		}
		
	}

	@NodePopupMethod(menuText="Print Finger Table")
	public void printFingerTable(){
		Tools.appendToOutput("\n\nnode: " + this.ID + " ( "+ this.getHashID() +" )");
		
		List<FingerEntry> fingerTable = this.getDht().getFingerTable();
		for (FingerEntry fingerEntry : fingerTable) {
			Tools.appendToOutput("\n"+fingerEntry.getIndex()+" | "+fingerEntry.getStartHash()+" --> "+fingerEntry.getSucessorNode().getHashID());
		}
	}
	
	@NodePopupMethod(menuText="Print Ring Information")
	public void printRingInfomation(){
		Tools.appendToOutput("\nnode: " + this.ID + " ( "+ this.getHashID() +" )");
		
		Tools.appendToOutput("\nnext: "+this.getDht().getNextNodeInChordRing().ID+" (hash: " + this.getDht().getNextNodeInChordRing().getHashID() + ")");
		Tools.appendToOutput("\nprevious: "+this.getDht().getPreviousNodeInChordRing().ID+" (hash: " + this.getDht().getPreviousNodeInChordRing().getHashID() + ")");
		Tools.appendToOutput("\n-----------------");
	}

	/*----------------------------------------------------
	---------------- GETTTERS AND SETTERS ----------------
	----------------------------------------------------*/

	public List<DataMessage> getDataMessages() {
		return dataMessages;
	}

	public void setDataMessages(List<DataMessage> dataMessages) {
		this.dataMessages = dataMessages;
	}

	public Map<Rules, List<Node>> getMapLocalMaliciousNodes() {
		return mapLocalMaliciousNodes;
	}

	public void setMapLocalMaliciousNodes(
			Map<Rules, List<Node>> mapLocalMaliciousNodes) {
		this.mapLocalMaliciousNodes = mapLocalMaliciousNodes;
	}

	public Integer getMonitorID() {
		return this.ID;
	}
	
	private void initHashChain(){
		Random rnd = Tools.getRandomNumberGenerator();
		for (int i = 0; i<hashLength; i++){
			hashChain[i] = rnd.nextInt();
		}
	}

	@Override
	public List<DataMessage> getDataMessage() {
		return this.dataMessages;
	}

	@Override
	public void setLocalMaliciousList(Rules rule, List<Node> lista) {
		
	}
	
	public Integer getHashID() {
		return this.dht.getHashID();
	}
	
	public void setHashID(Integer hashID) {
		this.getDht().setHashID(hashID);
	}
	
	public IDHT getDht() {
		return dht;
	}
	
	public void setDht(IDHT dht) {
		this.dht = dht;
	}
}
