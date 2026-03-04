// ============================================================
// 🔧 EDIT HERE: เปลี่ยนสี/สไตล์ปุ่ม Back ตรงนี้
// ============================================================
export default function BackButton({ onClick }) {
  return (
    <button
      onClick={onClick}
      style={{
        position: "absolute",
        top: "20px",
        left: "20px",
        background: "rgba(255,255,255,0.15)",
        backdropFilter: "blur(10px)",
        border: "1px solid rgba(255,255,255,0.3)",
        borderRadius: "50%",
        width: "48px",
        height: "48px",
        color: "#fff",
        fontSize: "22px",
        cursor: "pointer",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 100,
        transition: "all 0.2s",
      }}
      onMouseEnter={e => e.currentTarget.style.background = "rgba(255,255,255,0.3)"}
      onMouseLeave={e => e.currentTarget.style.background = "rgba(255,255,255,0.15)"}
    >
      ←
    </button>
  );
}
