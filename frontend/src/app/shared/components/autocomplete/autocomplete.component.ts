import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { debounceTime } from 'rxjs';

@Component({
  selector: 'app-autocomplete',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './autocomplete.component.html',
})
export class AutocompleteComponent {
  @Input() options: string[] = [];
  @Output() selected = new EventEmitter<string>();

  searchControl = new FormControl('');
  filteredOptions: string[] = [];
  showDropdown = false;

  ngOnInit() {
    this.filteredOptions = this.options;

    this.searchControl.valueChanges.pipe(debounceTime(200)).subscribe((value) => {
      const search = value?.toLowerCase() ?? '';
      this.filteredOptions = this.options.filter((opt) => opt.toLowerCase().includes(search));
      this.showDropdown = true;
    });
  }

  selectOption(option: string) {
    this.searchControl.setValue(option, { emitEvent: false });
    this.showDropdown = false;
    this.selected.emit(option);
  }
  hideDropdownLater() {
    setTimeout(() => {
      this.showDropdown = false;
    }, 1000);
  }
}
