import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamWorkload } from './team-workload';

describe('TeamWorkload', () => {
  let component: TeamWorkload;
  let fixture: ComponentFixture<TeamWorkload>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeamWorkload],
    }).compileComponents();

    fixture = TestBed.createComponent(TeamWorkload);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
