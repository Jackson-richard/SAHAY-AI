import { useSahay } from '../store/SahayContext.jsx'

export default function LanguageSelect({ id }) {
  const { state, setLanguage, supportedLanguages, languageNames } = useSahay()
  return (
    <select
      id={id}
      className="lang-select"
      aria-label="Language"
      value={state.preferredLanguage}
      onChange={(e) => setLanguage(e.target.value)}
    >
      {supportedLanguages.map((code) => (
        <option key={code} value={code}>{languageNames[code]}</option>
      ))}
    </select>
  )
}
