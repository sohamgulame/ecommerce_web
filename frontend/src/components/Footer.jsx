import React from 'react';

export default function Footer() {
  return (
    <footer className="bg-white border-t border-slate-200 mt-16 py-6">
      <div className="container-custom flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
        <div>
          <span className="font-semibold text-slate-700">SpringShop E-Commerce</span> &bull; Spring Boot 3 + React Architecture
        </div>
        <div className="flex gap-4">
          <span>Backend: REST / JPA / JWT</span>
          <span>&bull;</span>
          <span>Frontend: React / Axios / Tailwind</span>
        </div>
      </div>
    </footer>
  );
}
