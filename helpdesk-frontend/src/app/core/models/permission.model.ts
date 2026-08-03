export interface Permission {
  id?: number;
  name: string;
  description?: string;
}

export interface RolePermissions {
  roleId: number;
  roleName: string;
  permissionIds: number[];
}