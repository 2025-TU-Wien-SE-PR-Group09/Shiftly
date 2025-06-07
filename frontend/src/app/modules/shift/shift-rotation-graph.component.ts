import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Edge, Node } from '@swimlane/ngx-graph';
import { NgxGraphModule } from '@swimlane/ngx-graph';
import { curveLinear } from 'd3-shape';

@Component({
  selector: 'app-shift-rotation-graph',
  standalone: true,
  imports: [NgxGraphModule],
  templateUrl: './shift-rotation-graph.component.html',
})
export class ShiftRotationGraphComponent implements OnChanges {
  @Input() shifts: { week: string; label: string; employees: string[] }[] = [];
  nodes: Node[] = [];
  links: Edge[] = [];
  curve = curveLinear;

  private colorPalette = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b', '#8b5cf6', '#14b8a6', '#e11d48'];
  private workerColors = new Map<string, string>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['shifts']) {
      this.buildGraph();
    }
  }

  getWorkerColor(worker: string): string {
    return this.workerColors.get(worker) || '#000';
  }

  private buildGraph(): void {
    this.nodes = [];
    this.links = [];
    this.workerColors.clear();

    // Assign colors
    for (const shift of this.shifts) {
      for (const worker of shift.employees) {
        if (!this.workerColors.has(worker)) {
          const index = this.workerColors.size % this.colorPalette.length;
          this.workerColors.set(worker, this.colorPalette[index]);
        }
      }
    }

    // Build nodes and links
    for (let i = 0; i < this.shifts.length; i++) {
      const shift = this.shifts[i];
      const nodeId = `shift-${i}`;
      this.nodes.push({
        id: nodeId,
        data: {
          label: `${shift.label} (${shift.week})`,
          workers: shift.employees
        }
      });

      if (i < this.shifts.length - 1) {
        this.links.push({
          id: `link-${i}`,
          source: nodeId,
          target: `shift-${i + 1}`
        });
      }
    }
  }
}


