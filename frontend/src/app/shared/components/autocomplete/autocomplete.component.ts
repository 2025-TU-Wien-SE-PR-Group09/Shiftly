import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { debounceTime } from 'rxjs';

@Component({
  selector: 'app-autocomplete',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './autocomplete.component.html',
})
export class AutocompleteComponent implements OnInit {
  @Input() options: string[] = [];
  @Output() selected = new EventEmitter<string>();
  @Input() charsTyped = 3;

  searchControl = new FormControl('');
  filteredOptions: string[] = [];
  showDropdown = false;

  ngOnInit() {
    this.searchControl.valueChanges.pipe(debounceTime(200)).subscribe((value) => {
      const search = value?.toLowerCase() ?? '';

      // do not show the whole list when empty
      if (!search.trim()) {
        this.filteredOptions = [];
        this.showDropdown = false;
        return;
      }

      this.filteredOptions = this.options.filter((opt) => opt.toLowerCase().includes(search));
      this.showDropdown = this.filteredOptions.length > 0 && search.trim().length >= this.charsTyped;
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
