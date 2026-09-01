import { Link } from 'react-router-dom'

export default function Logo({ size = 36, className = 'logo-img' }) {
  return (
    <img
      src="/logo.png"
      alt="SAHAY-AI"
      width={size}
      height={size}
      className={className}
    />
  )
}

export function BrandLink() {
  return (
    <Link to="/" className="brand" style={{ textDecoration: 'none' }}>
      <Logo size={32} />
      <span>SAHAY-AI</span>
    </Link>
  )
}
