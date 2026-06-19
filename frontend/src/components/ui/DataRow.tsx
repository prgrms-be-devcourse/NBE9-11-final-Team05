interface DataRowProps {
    label: string;
    children: React.ReactNode;
  }
  
  export default function DataRow({ label, children }: DataRowProps) {
    return (
      <div className="flex items-start justify-between gap-4 py-1.5 text-sm">
        <span className="shrink-0 text-stone-500">{label}</span>
        <span className="text-right font-medium text-stone-800">{children}</span>
      </div>
    );
  }
  