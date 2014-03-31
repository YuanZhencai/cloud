package security;

public class PermissionIndex implements be.objectify.deadbolt.core.models.Permission{
	public int index;
	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return Integer.toString(index);
	}

}
