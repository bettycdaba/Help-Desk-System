export interface Role {
  id?: number;
  name: string;
  description?: string;
  active?: boolean;
  supervisorIds?: number[];
  supervisorNames?: string[];
}