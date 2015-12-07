package projects.topologyControl1.nodeDefinitions;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import projects.topologyControl1.Utils;
import projects.topologyControl1.enumerators.ChordMessageType;
/*import projects.ids_wsn.nodeDefinitions.energy.EnergyMode;
import projects.ids_wsn.nodeDefinitions.energy.IEnergy;*/
import projects.topologyControl1.nodeDefinitions.routing.IRouting;
import projects.topologyControl1.nodes.messages.ChordMessage;
import projects.topologyControl1.nodes.nodeImplementations.MonitorNode;
import projects.topologyControl1.nodes.timers.ChordDelayTimer;
import projects.topologyControl1.nodes.timers.RepeatSendMessageTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Node.NodePopupMethod;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public abstract class BasicNode extends Node{
	
	private Color myColor = Color.BLUE;
	private List<Integer> blackList = new Vector<Integer>();
	private Integer firstRoutingTtlRcv = 0;
	private IRouting routing;
	public int seqID = 0;
	public int beaconID = 0;
	/*private IEnergy bateria;
	*/
	/*public Float energy70 = 0f;
	public Float energy60 = 0f;
	public Float energy50 = 0f;
	public Float energy40 = 0f;
	
	public Boolean send70 = Boolean.FALSE;
	public Boolean send60 = Boolean.FALSE;
	public Boolean send50 = Boolean.FALSE;*/
	//private Boolean send20 = Boolean.FALSE;
	
	private Boolean isDead = Boolean.FALSE;
	private Boolean useFuzzyRouting = Boolean.FALSE;
	
	private int num_max_neighboors = 0;
	
	/**
	 * If a node is neighbor of a monitor, it must store its reference in that list
	 * because when the monitor is down, it will be able to advise the baseStation
	 */
	public Set<MonitorNode> monitors;
	
	
	
	/**
	 * Stores the interval that the neighbors of a monitor will check whether it's alive
	 */
	public static final Integer DELAY_TIME;
	public static final Integer MIN_DELAY_TIME = 100;
	public static final Integer MAX_DELAY_TIME = 200;
	
	static{
		Random random = Tools.getRandomNumberGenerator();
		int nextInt = Math.abs(random.nextInt());
		Integer interval = (nextInt % (MAX_DELAY_TIME.intValue() + 1));
		
		int delay = interval + (interval.intValue() < MIN_DELAY_TIME ? MIN_DELAY_TIME : 0);
		DELAY_TIME = delay;
		
	}

	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
		
	}

	@Override
	public void handleMessages(Inbox inbox) {
		preHandleMessage(inbox);
		
		if (this.isDead){
			return;
		}
		
		/*//Spent energy due to the listening mode
		this.bateria.spend(EnergyMode.LISTEN);
		*/
		while (inbox.hasNext()){
					
			Message message = inbox.next();
			
			/*//Message processing
			this.bateria.spend(EnergyMode.RECEIVE);*/
			
				preProcessingMessage(message);
				
			routing.receiveMessage(message);
			
				postProcessingMessage(message);
		}
		
		postHandleMessage(inbox);
		
	}

	@NodePopupMethod(menuText = "Show my power")
	public void showPower(){
		String r = "";
		r += this.ID + " => " + outgoingConnections.size() + " (" + getRadioIntensity()+ ")"+ "\n";
		infoBox(r, "");
	}
	
	@Override
	public void init() {
		this.setColor(getMyColor());
		this.monitors = new HashSet<MonitorNode>();
		try {
			//Here, we have to get the routing protocol from Config.xml and inject into routing attribute
			String routingProtocol = Configuration.getStringParameter("NetworkLayer/RoutingProtocolName");
			routing = Utils.StringToRoutingProtocol(routingProtocol);
			routing.setNode(this);
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Chave do protocolo de roteamento n�o foi encontrado");
			e.printStackTrace();
		}
		
		/*try {
			//Here, we have to get the battery implementation from Config.xml and inject into battery attribute
			String energyModel = Configuration.getStringParameter("Energy/EnergyModel");
			bateria = Utils.StringToEnergyModel(energyModel);
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Energy Model not found");
			e.printStackTrace();
		}*/
		
		try {
			String useFuzzy = Configuration.getStringParameter("NetworkLayer/UseFuzzyRouting");
			if (useFuzzy.equals("yes")){
				this.useFuzzyRouting = Boolean.TRUE;
			}
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Use Fuzzy Routing Key not found");
			e.printStackTrace();
		}
		
		/*energy70 = this.getBateria().getInitialEnergy() * 0.7f;
		energy60 = this.getBateria().getInitialEnergy() * 0.6f;
		energy50 = this.getBateria().getInitialEnergy() * 0.5f;
		energy40 = this.getBateria().getInitialEnergy() * 0.4f;*/
	}

	@Override
	public void neighborhoodChange() {
		
	}

	@Override
	public void postStep() {
		if (outgoingConnections.size() < this.num_max_neighboors || outgoingConnections.size() == 0){
			setRadioIntensity(getRadioIntensity()+0.2);
		}
	}

	@Override
	public void preStep() {
		
		//if i am dead, do not do anything
		if (this.isDead){
			return;
		}
		if (this.num_max_neighboors < outgoingConnections.size()){
			this.num_max_neighboors = outgoingConnections.size();
		}
		
		if (this.num_max_neighboors != 0){
			setRadioIntensity(getRadioIntensity()-0.1);
		}
		
		
	}

	public Color getMyColor() {
		return myColor;
	}

	public void setMyColor(Color myColor) {
		this.myColor = myColor;
	}

	public List<Integer> getBlackList() {
		return blackList;
	}

	public void setBlackList(Integer item) {
		blackList.add(item);
	}

	public Integer getFirstRoutingTtlRcv() {
		return firstRoutingTtlRcv;
	}

	public void setFirstRoutingTtlRcv(Integer firstRoutingTtlRcv) {
		this.firstRoutingTtlRcv = firstRoutingTtlRcv;
	}
	
	/*@NodePopupMethod(menuText="Send a message to the Base Station")
	public void sendMessageToBaseStation(){
		this.sendMessageToBaseStation(10);
	}*/
	
	public void sendMessageToBaseStation(Integer value){
		routing.sendMessage(value);
	}
	
	/*@NodePopupMethod(menuText="Send continuously a message to the Base Station per Round")
	public void sendMessageToBaseStationPerRound(){
		RepeatSendMessageTimer t = new RepeatSendMessageTimer(10);
		t.startRelative(1, this);
	}*/
	
	
	
/*	@NodePopupMethod(menuText="Print energy")
	public void printEnergy(){
		Tools.appendToOutput("Total spent energy: "+getBateria().getTotalSpentEnergy()+"\n");
		Tools.appendToOutput("Energy left: "+getBateria().getEnergy()+"\n");
	}*/
	
	public Boolean isNodeNextHop(Node destination){
		return routing.isNodeNextHop(destination);
	}
	
	/*@NodePopupMethod(menuText="Print Routing Table")
	public void printRoutingTable(){
		routing.printRoutingTable();		
	}*/
	
	/*@NodePopupMethod(menuText="Print Radio Intensity")
	public void printRadioIntensity(){
		Tools.appendToOutput("R.I: "+this.getRadioIntensity()+"\n");
	}*/
	
	
	/**
	 * This method is called before the Inbox iterator
	 */
	protected void preHandleMessage(Inbox inbox){}	
	
	/**
	 * This method is called before the end of the handleMessage method
	 */
	protected void postHandleMessage(Inbox inbox){}
	
	/**
	 * This method is called before the message processing in the handleMessage method
	 */
	protected void preProcessingMessage(Message message){}
	
	/**
	 * Every node that receive a chord message with the NOTIFY_NEIGHBORS value must add
	 * the sender of this message as a monitor
	 */
	protected void postProcessingMessage(Message message){
		if (message instanceof ChordMessage) {
			ChordMessage chordMessage = (ChordMessage) message;

			if(chordMessage.getChordMessageType().equals(ChordMessageType.NOTIFY_NEIGHBORS)){
				this.monitors.add((MonitorNode) chordMessage.getSender());
				
				if (monitors.size() == 1) {//se este n� for vizinho de pelo menos um monitor, � iniciado um timer (recursivo) para checar se o monitor est� vivo
					ChordDelayTimer chordDelayTimer = new ChordDelayTimer();
					chordDelayTimer.startRelative(BasicNode.DELAY_TIME, this);
				}
			}
		}
	}
	
	public Boolean beforeSendingMessage(Message message){ return Boolean.TRUE; }
	public void afterSendingMessage(Message message){}

/*	public IEnergy getBateria() {
		return bateria;
	}*/
/*
	public void setBateria(IEnergy bateria) {
		this.bateria = bateria;
	}*/
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		//super.drawAsDisk(g, pt, highlight, 10);
		String text = String.valueOf(this.ID);
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 8, Color.WHITE);
	}

	public IRouting getRouting() {
		return routing;
	}

	public Boolean getIsDead() {
		return isDead;
	}

	public Boolean getUseFuzzyRouting() {
		return useFuzzyRouting;
	}

	public void setIsDead(Boolean isDead) {
		this.isDead = isDead;
	}
	
	public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
}
