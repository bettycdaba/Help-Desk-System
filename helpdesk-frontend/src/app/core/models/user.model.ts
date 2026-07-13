export interface User {
  id?: number;
  employeeId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  active: boolean;
  password?: string;
  departmentId: number;
  departmentName?: string;
  roleIds: number[];
  roleNames?: string[];
}