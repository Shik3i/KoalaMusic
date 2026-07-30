# Accessibility

Acceptance criteria:

- every actionable icon has a localized semantic label;
- touch targets are at least 48dp without overlapping adjacent controls;
- 200% font scale does not hide primary actions or truncate status meaning;
- content order matches TalkBack, keyboard, and switch-access focus order;
- artwork descriptions are omitted when decorative and meaningful otherwise;
- state is not communicated by color alone and meets Material contrast;
- reduced-motion preference removes nonessential motion;
- phone portrait/landscape, compact/medium/expanded windows, tablets, foldables,
  keyboard navigation, and resizable ChromeOS windows remain usable;
- loading, empty, offline, error, and retry states are announced.

Accessibility semantics are part of UI tests and release review, not a final
polish pass.
