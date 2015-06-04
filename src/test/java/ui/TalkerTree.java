package ui;

import java.util.List;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;


import com.github.theholywaffle.lolchatapi.wrapper.ITalker;
import com.github.yeori.lol.muc.ChatRoom;
import com.github.yeori.lol.muc.Talker;

public class TalkerTree<E extends ITalker> extends JTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2631836358286523909L;


	public TalkerTree() {
		this.setModel(new FriendTreeModel());
	}
	
	public void addTalkerNode ( E talker) {
		
		addTalkerToTreeView(talker);
		
	}
	
	/**
	 * 친구를 friendTree에 그려넣음.
	 * @param paretNode 
	 * @param model2 
	 * @param f
	 */
	public void addTalkerToTreeView ( final E talker) {
		final DefaultTreeModel model = (DefaultTreeModel) this.getModel();
		final MutableTreeNode rootNode = (MutableTreeNode) model.getRoot();
		final DefaultMutableTreeNode node = new DefaultMutableTreeNode(talker);
		talker.getName(true);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				model.insertNodeInto(node, rootNode, 0);
				System.out.println(talker);
			}
		});
	}
	
	public void renderTalkers(List<E> talkers) {
		
		
		for ( final E t : talkers ) {
			addTalkerToTreeView( t );
		}
	}
	
	public DefaultMutableTreeNode findFriendNodeByJID ( String jid ) {
		FriendTreeModel model = (FriendTreeModel) this.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		int sz = root.getChildCount();
		for ( int i = 0 ; i < sz ; i++) {
			DefaultMutableTreeNode cNode = (DefaultMutableTreeNode) model.getChild(root, i);
			@SuppressWarnings("unchecked")
			E f = (E) cNode.getUserObject();
			if ( f.getUserId().equals ( jid ) ) {
				return cNode;
			}
		}
		throw new RuntimeException ( "no such friend node : " + jid);
	}
	
	public void removeTreeNode ( DefaultMutableTreeNode node) {
		FriendTreeModel model = (FriendTreeModel) this.getModel();
		model.removeNodeFromParent(node);
	}
	
	/**
	 * 친구를 friendTree에 그려넣음.
	 * @param paretNode 
	 * @param model2 
	 * @param f
	 */
	public void showTalkers(List<E> talkers) {
	
		for ( final E t : talkers ) {
			addTalkerToTreeView( t );
		}
	}
	
	@SuppressWarnings("unchecked")
	public void update(ChatRoom room) {
		FriendTreeModel model = getTreeModel();
		model.clear();
		
		List<Talker> talkers = room.getTalkers();
		for( Talker t : talkers) {
			addTalkerToTreeView((E) t );
		}
	}
	
	
	static class FriendTreeModel extends DefaultTreeModel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5027725396504020805L;
		public FriendTreeModel() {
			this( null, true);
		}
		public FriendTreeModel(TreeNode root) {
			this(root, true);
		}

		FriendTreeModel(TreeNode root, boolean asksAllowsChildren) {
			super(root, asksAllowsChildren);
			if ( root == null ) {
				setRoot(new DefaultMutableTreeNode("FRIENDS"));
			}
		}
		public void clear() {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
			root.removeAllChildren();
		}
		
		
	}


	public FriendTreeModel getTreeModel() {
		return (FriendTreeModel ) this.getModel();
	}
}
