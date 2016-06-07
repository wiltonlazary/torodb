package com.torodb.backend.d2r.model;

//TODO: Add constraint annotations and asserts
public class PathStack {

	private PathInfo top = null;

	public enum PathNodeType {
		Field, Object, Array, Idx
	};

	public PathStack() {
		this.top = new PathField("", null);
	}

	public PathInfo peek() {
		return top;
	}

	public PathInfo pop() {
		PathInfo topper = top;
		top = top.getParent();
		return topper;
	}

	public void pushField(String name) {
		top = top.appendField(name);
	}

	public void pushObject(RowInfo rowInfo) {
		top = top.appendObject(rowInfo);
	}

	public void pushArray() {
		if (top == null) {
			throw new IllegalArgumentException("Building an array on root document");
		}
		top = top.appendArray();
	}

	public void pushArrayIdx(int idx) {
		if (top == null) {
			throw new IllegalArgumentException("Building an array index on root document");
		} else if (top.getNodeType() != PathNodeType.Array) {
			throw new IllegalArgumentException("Building an array index on document");
		}
		top = ((PathArray) top).appendIdx(idx);
	}

	public void pushArrayIdx(int idx, RowInfo rowInfo) {
		if (top == null) {
			throw new IllegalArgumentException("Building an array index on root document");
		} else if (top.getNodeType() != PathNodeType.Array) {
			throw new IllegalArgumentException("Building an array index on document");
		}
		top = ((PathArray) top).appendIdx(idx, rowInfo);
	}

	public abstract class PathInfo {

		protected PathInfo parent;
		protected TableRefImpl tableRef;

		private PathInfo(PathInfo parent) {
			this.parent = parent;
		}

		PathObject appendObject(RowInfo rowInfo) {
			return new PathObject(this, rowInfo);
		}

		PathField appendField(String name) {
			return new PathField(name, this);
		}

		PathArray appendArray() {
			return new PathArray(1, this);
		}

		public TableRefImpl getTableRef(){
			return tableRef;
		}
		
		public PathInfo getParent() {
			return this.parent;
		}

		@Override
		public int hashCode() {
			return this.getPath().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof PathInfo))
				return false;
			PathInfo other = (PathInfo) obj;
			return this.getPath().equals(other.getPath());
		}

		public boolean is(PathNodeType type) {
			return getNodeType().equals(type);
		}

		public abstract RowInfo findParentRowInfo();
		
		public abstract String getPath();

		public abstract PathNodeType getNodeType();

	}

	public class PathField extends PathInfo {

		private String fieldName;
		private String path;

		private PathField(String fieldName, PathInfo parent) {
			super(parent);
			this.fieldName = fieldName;
			this.path = calcPath();
			if (parent==null){
				this.tableRef = TableRefImpl.createRoot();
			}else{
				this.tableRef = parent.getTableRef().createChild(fieldName);
			}
		}

		private String calcPath() {
			if (parent == null || parent.getPath().length()==0) {
				return getFieldName();
			}
			return parent.getPath() + "." + getFieldName();
		}

		public PathObject getParentObject() {
			return (PathObject) parent;
		}

		@Override
		public String getPath() {
			return path;
		}

		public String getFieldName() {
			return fieldName;
		}

		@Override
		public PathNodeType getNodeType() {
			return PathNodeType.Field;
		}

		@Override
		public String toString() {
			if (parent == null || parent.getPath().length()==0) {
				return getFieldName();
			}
			return parent.toString() + "." + getFieldName();
		}
		
		@Override
		public RowInfo findParentRowInfo() {
			if (parent!=null){
				return parent.findParentRowInfo();
			}
			return null;
		}
	}

	public class PathObject extends PathInfo {

		private RowInfo rowInfo;

		private PathObject(PathInfo parent, RowInfo rowInfo) {
			super(parent);
			this.rowInfo = rowInfo;
			this.tableRef = parent.getTableRef();
		}

		@Override
		public String getPath() {
			return parent.getPath();
		}

		@Override
		public PathNodeType getNodeType() {
			return PathNodeType.Object;
		}

		@Override
		public String toString() {
			return parent.toString();
		}
		
		public RowInfo findParentRowInfo() {
			return rowInfo;
		}		
	}

	public class PathArray extends PathInfo {

		private int level;
		private String path;

		private PathArray(int level, PathInfo parent) {
			super(parent);
			this.level = level;
			this.path = calcPath();
			if (level==1){
				this.tableRef = parent.getTableRef();
			}else{
				this.tableRef = parent.tableRef.createChild("$" + level);
			}
		}

		PathArrayIdx appendIdx(int idx) {
			return new PathArrayIdx(idx, this);
		}

		PathArrayIdx appendIdx(int idx, RowInfo rowInfo) {
			return new PathArrayIdx(idx, this, rowInfo);
		}

		@Override
		public String getPath() {
			return path;
		}

		private String calcPath() {
			if (level == 1) {
				return parent.getPath();
			}
			PathInfo noArray = parent;
			while (noArray.getNodeType() == PathNodeType.Array || noArray.getNodeType() == PathNodeType.Idx) {
				noArray = noArray.getParent();
			}
			return noArray.getPath() + "$" + level;
		}

		@Override
		public String toString() {
			return parent.toString();
		}

		@Override
		public PathNodeType getNodeType() {
			return PathNodeType.Array;
		}
		
		public RowInfo findParentRowInfo() {
			if (parent!=null){
				return parent.findParentRowInfo();
			}
			return null;
		}
	}

	// TODO maybe create to types: with and without rowinfo
	public class PathArrayIdx extends PathInfo {

		private int idx;
		private RowInfo rowInfo;

		private PathArrayIdx(int idx, PathInfo parent) {
			this(idx, parent, null);
		}

		private PathArrayIdx(int idx, PathInfo parent, RowInfo rowInfo) {
			super(parent);
			assert parent.getNodeType() == PathNodeType.Array;
			this.idx = idx;
			this.rowInfo = rowInfo;
			this.tableRef = parent.tableRef;
		}

		PathArray appendArray() {
			return new PathArray(((PathArray) parent).level + 1, this);
		}

		@Override
		public String getPath() {
			return parent.getPath();
		}

		@Override
		public PathNodeType getNodeType() {
			return PathNodeType.Idx;
		}

		@Override
		public String toString() {
			return parent.toString() + "[]";
		}

		public int getIdx() {
			return idx;
		}

		public RowInfo findParentRowInfo() {
			if (rowInfo != null) {
				return rowInfo;
			}
			if (parent!=null){
				return parent.findParentRowInfo();
			}
			return null;
		}
	}

}
